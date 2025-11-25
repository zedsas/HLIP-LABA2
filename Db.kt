import java.sql.Connection
import java.sql.DriverManager

object DbConfig {
    const val URL = "jdbc:h2:file:./scripts/appdb"
    const val USER = "sa"
    const val PASSWORD = ""
}

fun getConnection(): Connection {
    return DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD)
}