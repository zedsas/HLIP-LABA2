import kotlinx.cli.*
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

enum class Operation { READ, WRITE, EXECUTE }

data class Account(val salt: String, val hash: String)

val accounts = mapOf(
    "player" to Account(
        salt = "gameSalt",
        hash = computeHash("qwerty", "gameSalt")
    )
)

class Node(
    val id: String,
    val capacity: Int = 10,
    val ancestor: Node? = null
) {
    private val subNodes = mutableMapOf<String, Node>()
    private val accessRules = mutableMapOf<String, MutableSet<Operation>>()

    fun attach(node: Node) {
        subNodes[node.id] = node
    }

    fun retrieve(id: String): Node? = subNodes[id]

    fun locate(path: String): Node? {
        if (path.isEmpty()) return null
        val segments = path.split(".")
        for (segment in segments) {
            if (!isValidIdentifier(segment)) {
                return null
            }
        }
        var current: Node? = this
        for (segment in segments) {
            current = current?.retrieve(segment) ?: return null
        }
        return current
    }

    fun allow(user: String, op: Operation) {
        accessRules.computeIfAbsent(user) { mutableSetOf() }.add(op)
    }

    fun permitted(user: String, op: Operation): Boolean {
        val userOps = accessRules[user]
        return if (userOps != null && op in userOps) {
            true
        } else {
            ancestor?.permitted(user, op) ?: false
        }
    }

    companion object {
        fun isValidIdentifier(name: String): Boolean {
            if (name.isEmpty() || name.length > 20) return false
            return name.all { it.isLetterOrDigit() || it == '_' }
        }
    }
}

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

    if (resourceValue.split(".").any { !Node.isValidIdentifier(it) }) {
        exitProcess(7)
    }

    val volumeValue = volumeValueStr.toIntOrNull() ?: exitProcess(7)

    val account = accounts[loginValue] ?: exitProcess(3)

    if (computeHash(passwordValue, account.salt) != account.hash) {
        exitProcess(2)
    }

    val root = Node("system", 100)
    val level1 = Node("data", 50, root)
    val level2 = Node("logs", 20, level1)
    val leaf = Node("config", 10, level2)

    root.attach(level1)
    level1.attach(level2)
    level2.attach(leaf)

    level1.allow("player", Operation.READ)
    level2.allow("player", Operation.WRITE)
    leaf.allow("player", Operation.EXECUTE)

    val requestedOp = when (actionValue.lowercase()) {
        "read" -> Operation.READ
        "write" -> Operation.WRITE
        "execute" -> Operation.EXECUTE
        else -> exitProcess(4)
    }

    val target = root.locate(resourceValue) ?: exitProcess(6)

    if (!target.permitted(loginValue, requestedOp)) {
        exitProcess(5)
    }

    if (volumeValue > target.capacity) {
        exitProcess(8)
    }

    exitProcess(0)
}

fun computeHash(input: String, salt: String): String {
    val algorithm = MessageDigest.getInstance("SHA-256")
    val data = (salt + input).toByteArray(StandardCharsets.UTF_8)
    val digest = algorithm.digest(data)
    return digest.joinToString("") { "%02x".format(it) }
}