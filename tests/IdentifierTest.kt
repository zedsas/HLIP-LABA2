import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IdentifierTest {
    @Test
    fun validIdentifiers() {
        assertTrue(ResourceNode.isValidIdentifier("data"))
        assertTrue(ResourceNode.isValidIdentifier("logs_1"))
        assertTrue(ResourceNode.isValidIdentifier("A1"))
    }

    @Test
    fun invalidIdentifiers() {
        assertFalse(ResourceNode.isValidIdentifier(""))
        assertFalse(ResourceNode.isValidIdentifier("a-b"))
        assertFalse(ResourceNode.isValidIdentifier("this_identifier_is_definitely_too_long"))
    }
}
