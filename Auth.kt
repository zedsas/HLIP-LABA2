fun authenticate(login: String, password: String, accounts: Map<String, Account>): Int {
    val account = accounts[login] ?: return ExitCodes.INVALID_LOGIN
    return if (computeHash(password, account.salt) == account.hash) {
        ExitCodes.SUCCESS
    } else {
        ExitCodes.INVALID_PASSWORD
    }
}