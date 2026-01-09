import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.SQLNonTransientConnectionException
import java.sql.SQLException

@Service
class AuthService @Autowired constructor(
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    fun authenticate(login: String, password: String): Int {
        val account = try {
            accountRepository.findByLogin(login)
        } catch (e: SQLNonTransientConnectionException) {
            log.error("DB connection error during authentication, user={}", login, e)
            return ExitCodes.DB_CONNECTION_ERROR
        } catch (e: SQLException) {
            log.error("SQL error during authentication, user={}", login, e)
            return ExitCodes.SQL_ERROR
        }

        account ?: return ExitCodes.INVALID_LOGIN

        return if (computeHash(password, account.salt) == account.hash) {
            ExitCodes.SUCCESS
        } else {
            ExitCodes.INVALID_PASSWORD
        }
    }
}
