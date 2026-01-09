import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionJpaRepository : JpaRepository<PermissionEntity, Long> {
    fun findAllByUserId(userId: Long): List<PermissionEntity>
}
