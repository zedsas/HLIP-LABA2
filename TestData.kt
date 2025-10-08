val accounts = mapOf(
    "player" to Account(
        salt = "gameSalt",
        hash = computeHash("qwerty", "gameSalt")
    )
)

fun createTestResourceStructure(): ResourceNode {
    val root = ResourceNode("system", 100)
    val level1 = ResourceNode("data", 50, root)
    val level2 = ResourceNode("logs", 20, level1)
    val leaf = ResourceNode("config", 10, level2)

    root.addChild(level1)
    level1.addChild(level2)
    level2.addChild(leaf)

    return root
}

fun setupTestPermissions(aclService: AccessControlService, root: ResourceNode) {
    val level1 = root.findChild("data")!!
    val level2 = level1.findChild("logs")!!
    val leaf = level2.findChild("config")!!

    aclService.grant(level1, "player", Operation.READ)
    aclService.grant(level2, "player", Operation.WRITE)
    aclService.grant(leaf, "player", Operation.EXECUTE)
}