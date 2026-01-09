import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PathResolverTest {
    @Test
    fun resolveExistingPath() {
        val root = createTestResourceStructure()
        val node = PathResolver().resolveFrom(root, "data.logs.config")
        assertEquals("config", node?.id)
    }

    @Test
    fun resolveNonExistingPathReturnsNull() {
        val root = createTestResourceStructure()
        val node = PathResolver().resolveFrom(root, "data.nope")
        assertNull(node)
    }

    @Test
    fun invalidFormatReturnsNull() {
        val root = createTestResourceStructure()
        val node = PathResolver().resolveFrom(root, "invalid..path")
        assertNull(node)
    }
}
