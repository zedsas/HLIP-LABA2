import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ResourceTreeService @Autowired constructor(
    private val resourceRepository: ResourceJpaRepository
) {
    fun loadResourceTree(): ResourceNode {
        val entities = resourceRepository.findAll()

        val nodesById = mutableMapOf<Long, ResourceNode>()
        val parentIdById = mutableMapOf<Long, Long?>()

        for (e in entities) {
            val id = e.id ?: throw IllegalStateException("ResourceEntity.id is null")
            nodesById[id] = ResourceNode(e.title, e.capacity, null)
            parentIdById[id] = e.parent?.id
        }

        var root: ResourceNode? = null

        for ((id, node) in nodesById) {
            val parentId = parentIdById[id]
            if (parentId == null) {
                root = node
            } else {
                val parent = nodesById[parentId]
                    ?: throw IllegalStateException("Parent id=$parentId not found for id=$id")
                parent.addChild(node)
            }
        }

        return root ?: throw IllegalStateException("Root resource not found")
    }
}
