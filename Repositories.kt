class AccountRepository {

    fun findByLogin(login: String): Account? {
        val sql = "SELECT salt, password_hash FROM accounts WHERE login = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, login)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        Account(
                            salt = rs.getString("salt"),
                            hash = rs.getString("password_hash")
                        )
                    } else null
                }
            }
        }
    }
}

class ResourceRepository {

    data class ResourceRow(
        val id: String,
        val capacity: Int,
        val parentId: String?
    )

    fun loadResourceTree(): ResourceNode {
        val sql = "SELECT id, capacity, parent_id FROM resources"
        val rows = mutableListOf<ResourceRow>()

        getConnection().use { conn ->
            conn.createStatement().use { st ->
                st.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        rows += ResourceRow(
                            id = rs.getString("id"),
                            capacity = rs.getInt("capacity"),
                            parentId = rs.getString("parent_id")
                        )
                    }
                }
            }
        }

        val nodes = mutableMapOf<String, ResourceNode>()
        rows.forEach { row ->
            nodes[row.id] = ResourceNode(row.id, row.capacity, null)
        }

        var root: ResourceNode? = null

        rows.forEach { row ->
            val node = nodes[row.id]!!
            if (row.parentId == null) {
                root = node
            } else {
                val parent = nodes[row.parentId]
                    ?: throw IllegalStateException("Parent ${row.parentId} not found for ${row.id}")
                parent.addChild(node)
            }
        }

        return root ?: throw IllegalStateException("Root resource not found")
    }
}

class PermissionRepository {

    fun loadPermissions(aclService: AccessControlService, resourcesRoot: ResourceNode) {
        val index = mutableMapOf<String, ResourceNode>()

        fun fillIndex(node: ResourceNode) {
            index[node.id] = node
            node.children().forEach { fillIndex(it) }
        }
        fillIndex(resourcesRoot)

        val sql = "SELECT user_login, resource_id, operation FROM permissions"

        getConnection().use { conn ->
            conn.createStatement().use { st ->
                st.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        val user = rs.getString("user_login")
                        val resourceId = rs.getString("resource_id")
                        val opStr = rs.getString("operation")

                        val node = index[resourceId] ?: continue
                        val op = Operation.valueOf(opStr)

                        aclService.grant(node, user, op)
                    }
                }
            }
        }
    }
}