import kotlinx.cli.*
import kotlin.system.exitProcess

val accounts = mapOf(
    "player" to Account(
        salt = "gameSalt",
        hash = computeHash("qwerty", "gameSalt")
    )
)

fun printHelp() {
    println("Система контроля доступа к ресурсам")
    println("Использование: resource-access --login <user> --password <pass> --action <operation> --resource <path> --volume <amount>")
    println()
    println("Аргументы:")
    println("  --login <user>     Идентификатор пользователя")
    println("  --password <pass>  Секрет для аутентификации")
    println("  --action <operation> Запрашиваемая операция: read, write, execute")
    println("  --resource <path>  Путь к ресурсу через точки (например, A.B.C)")
    println("  --volume <amount>  Запрашиваемый объем ресурса (целое число)")
    println("  -h, --help        Показать эту справку")
    println()
    ExitCodes.printAllCodes()
}

data class ParsedArgs(
    val login: String,
    val password: String,
    val action: String,
    val resource: String,
    val volume: Int
)

fun parseArguments(args: Array<String>): ParsedArgs? {
    val parser = ArgParser("resource-access")

    val username by parser.option(ArgType.String, fullName = "login", description = "Идентификатор пользователя").required()
    val secret by parser.option(ArgType.String, fullName = "password", description = "Секрет для аутентификации").required()
    val operation by parser.option(ArgType.String, fullName = "action", description = "Запрашиваемая операция: read, write, execute").required()
    val path by parser.option(ArgType.String, fullName = "resource", description = "Путь к ресурсу через точки").required()
    val amount by parser.option(ArgType.String, fullName = "volume", description = "Запрашиваемый объем ресурса").required()

    try {
        parser.parse(args)
    } catch (e: Exception) {
        if (e.message?.contains("help") == true) {
            printHelp()
            exitProcess(ExitCodes.HELP_REQUESTED)
        }
        return null // invalid format
    }

    if (path.split(".").any { !ResourceNode.isValidIdentifier(it) }) {
        return null
    }

    val volumeValue = amount.toIntOrNull() ?: return null

    return ParsedArgs(username, secret, operation, path, volumeValue)
}

fun handleRequest(
    args: ParsedArgs,
    accounts: Map<String, Account>,
    root: ResourceNode,
    aclService: AccessControlService
): Int {

    val account = accounts[args.login] ?: return ExitCodes.INVALID_LOGIN
    if (computeHash(args.password, account.salt) != account.hash) {
        return ExitCodes.INVALID_PASSWORD
    }

    val requestedOp = when (args.action.lowercase()) {
        "read" -> Operation.READ
        "write" -> Operation.WRITE
        "execute" -> Operation.EXECUTE
        else -> return ExitCodes.UNKNOWN_OPERATION
    }

    val pathResolver = PathResolver()
    val target = pathResolver.resolveFrom(root, args.resource) ?: return ExitCodes.RESOURCE_NOT_FOUND

    if (!aclService.isPermitted(target, args.login, requestedOp)) {
        return ExitCodes.ACCESS_DENIED
    }

    if (args.volume > target.capacity) {
        return ExitCodes.EXCEEDED_CAPACITY
    }

    return ExitCodes.SUCCESS
}

fun createTestResourceStructure(): ResourceNode {
    val root = ResourceNode("system", 100)
    val level1 = ResourceNode("data", 50, root)
    val level2 = ResourceNode("logs", 20, level1)
    val leaf = ResourceNode("config", 10, level2)

    root.addChild(level1)
    level1.addChild(level2)
    level2.addChild(leaf)

    return root
}

fun setupTestPermissions(aclService: AccessControlService, root: ResourceNode) {
    val level1 = root.findChild("data")!!
    val level2 = level1.findChild("logs")!!
    val leaf = level2.findChild("config")!!

    aclService.grant(level1, "player", Operation.READ)
    aclService.grant(level2, "player", Operation.WRITE)
    aclService.grant(leaf, "player", Operation.EXECUTE)
}

fun main(args: Array<String>) {
    if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
        printHelp()
        exitProcess(ExitCodes.HELP_REQUESTED)
    }

    val parsed = parseArguments(args) ?: run {
        exitProcess(ExitCodes.INVALID_FORMAT)
    }

    val root = createTestResourceStructure()
    val aclService = AccessControlService()
    setupTestPermissions(aclService, root)

    val exitCode = handleRequest(parsed, accounts, root, aclService)
    exitProcess(exitCode)
}