import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionLoaderService @Autowired constructor(
    private val permissionRepository: PermissionJpaRepository
) {
    fun loadPermissions(aclService: AccessControlService, resourcesRoot: ResourceNode) {
        val index = mutableMapOf<String, ResourceNode>()

        fun fillIndex(node: ResourceNode) {
            index[node.id] = node
            node.children().forEach { fillIndex(it) }
        }
        fillIndex(resourcesRoot)

        val permissions = permissionRepository.findAll()
        for (p in permissions) {
            val username = p.user.username
            val resourceTitle = p.resource.title
            val node = index[resourceTitle] ?: continue
            aclService.grant(node, username, p.operation)
        }
    }
}
