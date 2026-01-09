import kotlinx.cli.*
import kotlin.system.exitProcess

fun printHelp() {
    println("Система контроля доступа к ресурсам")
    println("Использование: resource-access --login <user> --password <pass> --action <operation> --resource <path> --volume <amount>")
    println()
    println("Аргументы:")
    println("  --login <user>       Идентификатор пользователя")
    println("  --password <pass>    Секрет для аутентификации")
    println("  --action <operation> Запрашиваемая операция: read, write, execute")
    println("  --resource <path>    Путь к ресурсу через точки (например, system.data.logs)")
    println("  --volume <amount>    Запрашиваемый объем ресурса (целое число)")
    println("  -h, --help           Показать эту справку")
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
