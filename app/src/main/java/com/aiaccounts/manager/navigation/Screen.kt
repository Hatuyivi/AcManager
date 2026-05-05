package com.aiaccounts.manager.navigation

sealed class Screen {
    object Empty : Screen()
    data class Web(val accountId: String) : Screen()
}
