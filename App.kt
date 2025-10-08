import kotlinx.cli.*
import kotlin.system.exitProcess

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
    if (args.contains("-h") || args.contains("--help")) {
        printHelp()
        exitProcess(ExitCodes.HELP_REQUESTED)
    }

    val parser = ArgParser("resource-access")
    val username by parser.option(ArgType.String, fullName = "login").required()
    val secret by parser.option(ArgType.String, fullName = "password").required()
    val operation by parser.option(ArgType.String, fullName = "action").required()
    val path by parser.option(ArgType.String, fullName = "resource").required()
    val amount by parser.option(ArgType.String, fullName = "volume").required()

    try {
        parser.parse(args)
    } catch (e: Exception) {
        return null
    }

    if (path.split(".").any { !ResourceNode.isValidIdentifier(it) }) return null
    val volumeValue = amount.toIntOrNull() ?: return null

    return ParsedArgs(username, secret, operation, path, volumeValue)
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
        exitProcess(ExitCodes.HELP_REQUESTED)
    }

    val parsed = parseArguments(args) ?: exitProcess(ExitCodes.INVALID_FORMAT)

    val root = createTestResourceStructure()
    val acl = AccessControlService()
    setupTestPermissions(acl, root)

    val authResult = authenticate(parsed.login, parsed.password, accounts)
    if (authResult != ExitCodes.SUCCESS) {
        exitProcess(authResult)
    }

    val op = parseOperation(parsed.action) ?: exitProcess(ExitCodes.UNKNOWN_OPERATION)

    val result = checkAccess(parsed.login, op, parsed.resource, parsed.volume, root, acl)
    exitProcess(result)
}