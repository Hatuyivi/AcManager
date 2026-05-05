package com.aiaccounts.manager

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiaccounts.manager.data.AccountRepository
import com.aiaccounts.manager.navigation.Screen
import com.aiaccounts.manager.ui.AccountListScreen
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

                val currentScreen = remember { mutableStateOf<Screen>(Screen.AccountList) }

                when (val screen = currentScreen.value) {
                    is Screen.AccountList -> {
                        AccountListScreen(
                            accounts = accounts,
                            activeAccountId = activeAccountId,
                            onSelectAccount = { id ->
                                viewModel.selectAccount(id)
                            },
                            onDeleteAccount = { id ->
                                viewModel.deleteAccount(id)
                            },
                            onAddAccount = { name, platform, url ->
                                viewModel.addAccount(name, platform, url)
                            },
                            onOpenWebView = { accountId ->
                                viewModel.selectAccount(accountId)
                                currentScreen.value = Screen.Web(accountId)
                            }
                        )
                    }

                    is Screen.Web -> {
                        val account = accounts.find { it.id == screen.accountId }
                        if (account != null) {
                            WebViewScreen(
                                account = account,
                                onBack = {
                                    currentScreen.value = Screen.AccountList
                                },
                                onMessageSent = {
                                    viewModel.incrementMessageCount(screen.accountId)
                                }
                            )
                        } else {
                            currentScreen.value = Screen.AccountList
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
