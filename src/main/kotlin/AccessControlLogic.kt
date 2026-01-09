import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

fun parseOperation(action: String): Operation? = when (action.lowercase()) {
    "read" -> Operation.READ
    "write" -> Operation.WRITE
    "execute" -> Operation.EXECUTE
    else -> null
}

@Service
class AccessCheckService @Autowired constructor() {

    fun checkAccess(
        login: String,
        operation: Operation,
        resourcePath: String,
        volume: Int,
        root: ResourceNode,
        aclService: AccessControlService
    ): Int {
        val target = PathResolver().resolveFrom(root, resourcePath)
            ?: return ExitCodes.RESOURCE_NOT_FOUND

        if (!aclService.isPermitted(target, login, operation)) {
            return ExitCodes.ACCESS_DENIED
        }

        if (volume > target.capacity) {
            return ExitCodes.EXCEEDED_CAPACITY
        }

        return ExitCodes.SUCCESS
    }
}
