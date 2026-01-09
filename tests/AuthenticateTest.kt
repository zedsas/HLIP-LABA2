import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthenticateTest {
    @Test
    fun validLoginValidPasswordSuccess() {
        val code = authenticate("player", "qwerty", accounts)
        assertEquals(ExitCodes.SUCCESS, code)
    }

    @Test
    fun validLoginWrongPasswordInvalidPassword() {
        val code = authenticate("player", "wrongpass", accounts)
        assertEquals(ExitCodes.INVALID_PASSWORD, code)
    }

    @Test
    fun unknownLoginInvalidLogin() {
        val code = authenticate("unknown_user", "qwerty", accounts)
        assertEquals(ExitCodes.INVALID_LOGIN, code)
    }
}
