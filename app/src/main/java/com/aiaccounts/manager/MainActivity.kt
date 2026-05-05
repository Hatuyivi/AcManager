package com.aiaccounts.manager

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiaccounts.manager.data.AccountRepository
import com.aiaccounts.manager.navigation.Screen
import com.aiaccounts.manager.ui.EmptyScreen
import com.aiaccounts.manager.ui.WebViewScreen
import com.aiaccounts.manager.ui.theme.AIAccountManagerTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { AccountRepository(applicationContext) }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView.setWebContentsDebuggingEnabled(false)
        enableEdgeToEdge()

        setContent {
            AIAccountManagerTheme {
                val accounts by viewModel.accounts.collectAsStateWithLifecycle()
                val activeAccountId by viewModel.activeAccountId.collectAsStateWithLifecycle()
                val currentScreen = remember { mutableStateOf<Screen>(Screen.Empty) }

                LaunchedEffect(accounts, activeAccountId) {
                    if (accounts.isEmpty()) {
                        currentScreen.value = Screen.Empty
                    } else {
                        val screen = currentScreen.value
                        when {
                            screen is Screen.Web && accounts.any { it.id == screen.accountId } -> Unit
                            activeAccountId != null && accounts.any { it.id == activeAccountId } ->
                                currentScreen.value = Screen.Web(activeAccountId!!)
                            else ->
                                currentScreen.value = Screen.Web(accounts.first().id)
                        }
                    }
                }

                when (val screen = currentScreen.value) {
                    is Screen.Empty -> {
                        EmptyScreen(
                            onAddAccount = { name, platform, url ->
                                viewModel.addAccount(name, platform, url)
                            }
                        )
                    }

                    is Screen.Web -> {
                        val account = accounts.find { it.id == screen.accountId }
                        if (account != null) {
                            WebViewScreen(
                                accounts = accounts,
                                activeAccountId = activeAccountId,
                                account = account,
                                onSwitchAccount = { id ->
                                    viewModel.selectAccount(id)
                                    currentScreen.value = Screen.Web(id)
                                },
                                onDeleteAccount = { id ->
                                    viewModel.deleteAccount(id)
                                    val remaining = accounts.filter { it.id != id }
                                    currentScreen.value = if (remaining.isEmpty()) {
                                        Screen.Empty
                                    } else {
                                        Screen.Web(remaining.first().id)
                                    }
                                },
                                onAddAccount = { name, platform, url ->
                                    viewModel.addAccount(name, platform, url)
                                },
                                onNewAccountOpened = { newId ->
                                    viewModel.selectAccount(newId)
                                    currentScreen.value = Screen.Web(newId)
                                },
                                onMessageSent = {
                                    viewModel.incrementMessageCount(screen.accountId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
