import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService @Autowired constructor(
    private val accountRepository: AccountJpaRepository
) {
    fun authenticate(username: String, password: String): Int {
        val account = accountRepository.findByUsername(username) ?: return ExitCodes.INVALID_LOGIN
        return if (computeHash(password, account.salt) == account.passwordHash) {
            ExitCodes.SUCCESS
        } else {
            ExitCodes.INVALID_PASSWORD
        }
    }
}
