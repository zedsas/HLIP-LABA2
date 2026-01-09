import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class OperationAndAccessTest {
    @Test
    fun parseOperationWorks() {
        assertEquals(Operation.READ, parseOperation("read"))
        assertEquals(Operation.WRITE, parseOperation("WRITE"))
        assertEquals(Operation.EXECUTE, parseOperation("Execute"))
        assertNull(parseOperation("delete"))
    }

    @Test
    fun checkAccessSuccess() {
        val root = createTestResourceStructure()
        val acl = AccessControlService()
        setupTestPermissions(acl, root)

        val code = checkAccess("player", Operation.READ, "data", 1, root, acl)
        assertEquals(ExitCodes.SUCCESS, code)
    }

    @Test
    fun checkAccessDenied() {
        val root = createTestResourceStructure()
        val acl = AccessControlService()
        setupTestPermissions(acl, root)

        val code = checkAccess("player", Operation.EXECUTE, "data", 1, root, acl)
        assertEquals(ExitCodes.ACCESS_DENIED, code)
    }

    @Test
    fun checkAccessResourceNotFound() {
        val root = createTestResourceStructure()
        val acl = AccessControlService()
        setupTestPermissions(acl, root)

        val code = checkAccess("player", Operation.READ, "nonexistent", 1, root, acl)
        assertEquals(ExitCodes.RESOURCE_NOT_FOUND, code)
    }

    @Test
    fun checkAccessExceededCapacity() {
        val root = createTestResourceStructure()
        val acl = AccessControlService()
        setupTestPermissions(acl, root)

        val code = checkAccess("player", Operation.READ, "data", 100, root, acl)
        assertEquals(ExitCodes.EXCEEDED_CAPACITY, code)
    }
}
