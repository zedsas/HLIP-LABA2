import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class HashTest {
    @Test
    fun sameInputSameSaltSameHash() {
        val h1 = computeHash("qwerty", "gameSalt")
        val h2 = computeHash("qwerty", "gameSalt")
        assertEquals(h1, h2)
    }

    @Test
    fun differentSaltDifferentHash() {
        val h1 = computeHash("qwerty", "salt1")
        val h2 = computeHash("qwerty", "salt2")
        assertNotEquals(h1, h2)
    }
}
