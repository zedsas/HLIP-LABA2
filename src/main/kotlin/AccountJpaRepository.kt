import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountJpaRepository : JpaRepository<AccountEntity, Long> {
    fun findByUsername(username: String): AccountEntity?
}
