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

sealed class ParseResult {
    data class Ok(val args: ParsedArgs) : ParseResult()
    object HelpRequested : ParseResult()
    object InvalidFormat : ParseResult()
}

fun parseArguments(args: Array<String>): ParseResult {
    if (args.contains("-h") || args.contains("--help")) {
        return ParseResult.HelpRequested
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
        return ParseResult.InvalidFormat
    }

    if (path.split(".").any { !ResourceNode.isValidIdentifier(it) }) {
        return ParseResult.InvalidFormat
    }

    val volumeValue = amount.toIntOrNull() ?: return ParseResult.InvalidFormat

    return ParseResult.Ok(
        ParsedArgs(
            login = username,
            password = secret,
            action = operation,
            resource = path,
            volume = volumeValue
        )
    )
}

fun runApplication(args: Array<String>): Int {
    if (args.isEmpty()) {
        printHelp()
        return ExitCodes.HELP_REQUESTED
    }

    return when (val parsed = parseArguments(args)) {
        is ParseResult.HelpRequested -> {
            printHelp()
            ExitCodes.HELP_REQUESTED
        }

        is ParseResult.InvalidFormat -> ExitCodes.INVALID_FORMAT

        is ParseResult.Ok -> {
            val p = parsed.args

            val root = createTestResourceStructure()
            val acl = AccessControlService()
            setupTestPermissions(acl, root)

            val authResult = authenticate(p.login, p.password, accounts)
            if (authResult != ExitCodes.SUCCESS) return authResult

            val op = parseOperation(p.action) ?: return ExitCodes.UNKNOWN_OPERATION

            checkAccess(
                login = p.login,
                operation = op,
                resourcePath = p.resource,
                volume = p.volume,
                root = root,
                aclService = acl
            )
        }
    }
}

fun main(args: Array<String>) {
    exitProcess(runApplication(args))
}
