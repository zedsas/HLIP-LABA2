import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.system.exitProcess
import java.sql.SQLNonTransientConnectionException
import java.sql.SQLException

@Component
class CliRunner @Autowired constructor(
    private val resourceRepo: ResourceRepository,
    private val permissionRepo: PermissionRepository,
    private val authService: AuthService,
    private val aclService: AccessControlService,
    private val accessCheckService: AccessCheckService
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(CliRunner::class.java)

    override fun run(vararg args: String) {
        log.info("Application started")

        if (args.contains("--calc-hash")) {
            val hash = computeHash("qwerty", "gameSalt")
            log.info("calc-hash requested")
            println(hash)
            return
        }

        if (args.isEmpty()) {
            log.info("No args, printing help")
            printHelp()
            exitProcess(ExitCodes.HELP_REQUESTED)
        }

        val parsed = parseArguments(args as Array<String>)
            ?: run {
                log.warn("Invalid arguments format: {}", args.joinToString(" "))
                exitProcess(ExitCodes.INVALID_FORMAT)
            }

        val root: ResourceNode = try {
            resourceRepo.loadResourceTree()
        } catch (e: SQLNonTransientConnectionException) {
            log.error("DB connection error while loading resources", e)
            exitProcess(ExitCodes.DB_CONNECTION_ERROR)
        } catch (e: SQLException) {
            log.error("SQL error while loading resources", e)
            exitProcess(ExitCodes.SQL_ERROR)
        }

        try {
            permissionRepo.loadPermissions(aclService, root)
        } catch (e: SQLNonTransientConnectionException) {
            log.error("DB connection error while loading permissions", e)
            exitProcess(ExitCodes.DB_CONNECTION_ERROR)
        } catch (e: SQLException) {
            log.error("SQL error while loading permissions", e)
            exitProcess(ExitCodes.SQL_ERROR)
        }

        val authCode = authService.authenticate(parsed.login, parsed.password)
        if (authCode != ExitCodes.SUCCESS) {
            log.warn("Authentication failed for user={}, exitCode={}", parsed.login, authCode)
            exitProcess(authCode)
        }

        val op = parseOperation(parsed.action)
            ?: run {
                log.warn("Unknown operation: {}", parsed.action)
                exitProcess(ExitCodes.UNKNOWN_OPERATION)
            }

        val result = accessCheckService.checkAccess(parsed.login, op, parsed.resource, parsed.volume, root, aclService)
        log.info(
            "Access check finished: user={}, op={}, resource={}, volume={}, exitCode={}",
            parsed.login, op, parsed.resource, parsed.volume, result
        )
        exitProcess(result)
    }
}
