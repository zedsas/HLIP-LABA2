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
    val parser = ArgParser("resource-access")

    val username by parser.option(ArgType.String, fullName = "login", description = "Идентификатор пользователя").required()
    val secret by parser.option(ArgType.String, fullName = "password", description = "Секрет для аутентификации").required()
    val operation by parser.option(ArgType.String, fullName = "action", description = "Запрашиваемая операция: read, write, execute").required()
    val path by parser.option(ArgType.String, fullName = "resource", description = "Путь к ресурсу через точки").required()
    val amount by parser.option(ArgType.String, fullName = "volume", description = "Запрашиваемый объем ресурса").required()

    if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
        printHelp()
        exitProcess(ExitCodes.HELP_REQUESTED)
    }

    try {
        parser.parse(args)
    } catch (e: Exception) {
        when {
            e.message?.contains("help") == true -> {
                printHelp()
                exitProcess(ExitCodes.HELP_REQUESTED)
            }
            else -> {
                exitProcess(ExitCodes.INVALID_FORMAT)
            }
        }
    }

    val loginValue = username
    val passwordValue = secret
    val actionValue = operation
    val resourceValue = path
    val volumeValueStr = amount

    if (resourceValue.split(".").any { !ResourceNode.isValidIdentifier(it) }) {
        exitProcess(ExitCodes.INVALID_FORMAT)
    }

    val volumeValue = volumeValueStr.toIntOrNull() ?: run {
        exitProcess(ExitCodes.INVALID_FORMAT)
    }

    val account = accounts[loginValue] ?: run {
        exitProcess(ExitCodes.INVALID_LOGIN)
    }

    if (computeHash(passwordValue, account.salt) != account.hash) {
        exitProcess(ExitCodes.INVALID_PASSWORD)
    }

    val root = createTestResourceStructure()
    val aclService = AccessControlService()
    setupTestPermissions(aclService, root)

    val requestedOp = when (actionValue.lowercase()) {
        "read" -> Operation.READ
        "write" -> Operation.WRITE
        "execute" -> Operation.EXECUTE
        else -> exitProcess(ExitCodes.UNKNOWN_OPERATION)
    }

    val pathResolver = PathResolver()
    val target = pathResolver.resolveFrom(root, resourceValue) ?: run {
        exitProcess(ExitCodes.RESOURCE_NOT_FOUND)
    }

    if (!aclService.isPermitted(target, loginValue, requestedOp)) {
        exitProcess(ExitCodes.ACCESS_DENIED)
    }

    if (volumeValue > target.capacity) {
        exitProcess(ExitCodes.EXCEEDED_CAPACITY)
    }

    exitProcess(ExitCodes.SUCCESS)
}