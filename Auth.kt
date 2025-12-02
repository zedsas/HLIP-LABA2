fun authenticate(login: String, password: String, accountRepository: AccountRepository): Int {
    val account = try {
        accountRepository.findByLogin(login)
    } catch (e: java.sql.SQLNonTransientConnectionException) {
        return ExitCodes.DB_CONNECTION_ERROR
    } catch (e: java.sql.SQLException) {
        return ExitCodes.SQL_ERROR
    }

    account ?: return ExitCodes.INVALID_LOGIN

    return if (computeHash(password, account.salt) == account.hash) {
        ExitCodes.SUCCESS
    } else {
        ExitCodes.INVALID_PASSWORD
    }
}