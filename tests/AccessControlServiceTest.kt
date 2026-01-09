import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AccessControlServiceTest {
    @Test
    fun permissionOnNodeAppliesToThatNode() {
        val root = createTestResourceStructure()
        val acl = AccessControlService()
        val data = root.findChild("data")!!
        acl.grant(data, "player", Operation.READ)

        assertTrue(acl.isPermitted(data, "player", Operation.READ))
        assertFalse(acl.isPermitted(data, "player", Operation.WRITE))
    }

    @Test
    fun permissionInheritedFromParent() {
        val root = createTestResourceStructure()
        val acl = AccessControlService()
        val data = root.findChild("data")!!
        val logs = data.findChild("logs")!!
        val config = logs.findChild("config")!!

        acl.grant(logs, "player", Operation.WRITE)

        assertTrue(acl.isPermitted(config, "player", Operation.WRITE))
        assertTrue(acl.isPermitted(logs, "player", Operation.WRITE))
        assertFalse(acl.isPermitted(data, "player", Operation.WRITE))
    }
}
