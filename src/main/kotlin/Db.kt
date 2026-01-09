import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager

@Component
class Db(
    private val config: DbConfig = DbConfig
) {
    fun getConnection(): Connection {
        return DriverManager.getConnection(config.url, config.user, config.password)
    }
}

object DbConfig {
    const val url = "jdbc:h2:file:./scripts/appdb"
    const val user = "sa"
    const val password = ""
}
