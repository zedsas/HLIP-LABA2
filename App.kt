import kotlinx.cli.*
import kotlin.system.exitProcess

data class Account(val salt: String, val hash: String)

val accounts = mapOf(
    "player" to Account(
        salt = "gameSalt",
        hash = computeHash("qwerty", "gameSalt")
    )
)

fun main(args: Array<String>) {
    val parser = ArgParser("resource-access")

    val username by parser.option(ArgType.String, fullName = "login", description = "Идентификатор пользователя").required()
    val secret by parser.option(ArgType.String, fullName = "password", description = "Секрет для аутентификации").required()
    val operation by parser.option(ArgType.String, fullName = "action", description = "Запрашиваемая операция: read, write, execute").required()
    val path by parser.option(ArgType.String, fullName = "resource", description = "Путь к ресурсу через точки").required()
    val amount by parser.option(ArgType.String, fullName = "volume", description = "Запрашиваемый объем ресурса").required()

    if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
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
        println("Коды возврата:")
        println("  0: Успешное выполнение")
        println("  1: Запрошена справка")
        println("  2: Неверный пароль")
        println("  3: Неверный логин")
        println("  4: Неизвестное действие над ресурсом")
        println("  5: Нет доступа")
        println("  6: Несуществующий ресурс")
        println("  7: Некорректный формат ресурса или объема")
        println("  8: Превышение максимального объема")
        exitProcess(1)
    }

    try {
        parser.parse(args)
    } catch (e: Exception) {
        when {
            e.message?.contains("help") == true -> {
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
                println("Коды возврата:")
                println("  0: Успешное выполнение")
                println("  1: Запрошена справка")
                println("  2: Неверный пароль")
                println("  3: Неверный логин")
                println("  4: Неизвестное действие над ресурсом")
                println("  5: Нет доступа")
                println("  6: Несуществующий ресурс")
                println("  7: Некорректный формат ресурса или объема")
                println("  8: Превышение максимального объема")
                exitProcess(1)
            }
            else -> {
                exitProcess(1)
            }
        }
    }

    val loginValue = username
    val passwordValue = secret
    val actionValue = operation
    val resourceValue = path
    val volumeValueStr = amount

    if (resourceValue.split(".").any { !ResourceNode.isValidIdentifier(it) }) {
        exitProcess(7)
    }

    val volumeValue = volumeValueStr.toIntOrNull() ?: exitProcess(7)

    val account = accounts[loginValue] ?: exitProcess(3)

    if (computeHash(passwordValue, account.salt) != account.hash) {
        exitProcess(2)
    }

    val root = ResourceNode("system", 100)
    val level1 = ResourceNode("data", 50, root)
    val level2 = ResourceNode("logs", 20, level1)
    val leaf = ResourceNode("config", 10, level2)

    root.addChild(level1)
    level1.addChild(level2)
    level2.addChild(leaf)

    val aclService = AccessControlService()
    aclService.grant(level1, "player", Operation.READ)
    aclService.grant(level2, "player", Operation.WRITE)
    aclService.grant(leaf, "player", Operation.EXECUTE)

    val requestedOp = when (actionValue.lowercase()) {
        "read" -> Operation.READ
        "write" -> Operation.WRITE
        "execute" -> Operation.EXECUTE
        else -> exitProcess(4)
    }

    val pathResolver = PathResolver()
    val target = pathResolver.resolveFrom(root, resourceValue) ?: exitProcess(6)

    if (!aclService.isPermitted(target, loginValue, requestedOp)) {
        exitProcess(5)
    }

    if (volumeValue > target.capacity) {
        exitProcess(8)
    }

    exitProcess(0)
}