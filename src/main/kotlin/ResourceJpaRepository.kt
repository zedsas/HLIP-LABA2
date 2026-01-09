import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceJpaRepository : JpaRepository<ResourceEntity, Long> {
    fun existsByTitle(title: String): Boolean
}
