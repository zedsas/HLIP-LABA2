import java.security.MessageDigest
import java.nio.charset.StandardCharsets

enum class Operation { READ, WRITE, EXECUTE }

data class Account(val salt: String, val hash: String)

class ResourceNode(
    val id: String,
    val capacity: Int = 10,
    var parent: ResourceNode? = null
) {
    private val children = mutableMapOf<String, ResourceNode>()

    fun addChild(node: ResourceNode) {
        children[node.id] = node
        node.parent = this
    }

    fun findChild(id: String): ResourceNode? = children[id]

    fun children(): Collection<ResourceNode> = children.values

    companion object {
        fun isValidIdentifier(name: String): Boolean {
            if (name.isEmpty() || name.length > 20) return false
            return name.all { it.isLetterOrDigit() || it == '_' }
        }
    }
}

class AccessControlList {
    private val rules = mutableMapOf<String, MutableSet<Operation>>()

    fun grant(user: String, operation: Operation) {
        rules.getOrPut(user) { mutableSetOf() }.add(operation)
    }

    fun isAllowed(user: String, operation: Operation): Boolean {
        return operation in (rules[user] ?: emptySet())
    }
}

class AccessControlService {
    private val nodeAcls = mutableMapOf<ResourceNode, AccessControlList>()

    fun grant(node: ResourceNode, user: String, operation: Operation) {
        nodeAcls.getOrPut(node) { AccessControlList() }.grant(user, operation)
    }

    fun isPermitted(node: ResourceNode, user: String, operation: Operation): Boolean {
        var current: ResourceNode? = node
        while (current != null) {
            val acl = nodeAcls[current]
            if (acl?.isAllowed(user, operation) == true) {
                return true
            }
            current = current.parent
        }
        return false
    }
}

class PathResolver {
    fun resolveFrom(start: ResourceNode, path: String): ResourceNode? {
        if (path.isEmpty()) return null
        val segments = path.split(".")
        if (segments.any { !ResourceNode.isValidIdentifier(it) }) return null

        var current: ResourceNode = start
        for (segment in segments) {
            current = current.findChild(segment) ?: return null
        }
        return current
    }
}

fun computeHash(input: String, salt: String): String {
    val algorithm = MessageDigest.getInstance("SHA-256")
    val data = (salt + input).toByteArray(StandardCharsets.UTF_8)
    val digest = algorithm.digest(data)
    return digest.joinToString("") { "%02x".format(it) }
}