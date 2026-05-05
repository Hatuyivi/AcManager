package com.aiaccounts.manager.navigation

sealed class Screen {
    object AccountList : Screen()
    data class Web(val accountId: String) : Screen()
}
