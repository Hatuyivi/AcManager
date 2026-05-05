package com.aiaccounts.manager.ui

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aiaccounts.manager.model.Account
import com.aiaccounts.manager.model.Platform
import com.aiaccounts.manager.webview.WebViewManager

@Composable
fun WebViewScreen(
    accounts: List<Account>,
    activeAccountId: String?,
    account: Account,
    onSwitchAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onAddAccount: (name: String, platform: Platform, url: String) -> Unit,
    onNewAccountOpened: (newId: String) -> Unit,
    onMessageSent: () -> Unit
) {
    val accentColor = account.platform.accentColor()

    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Track which account is currently loaded in the WebView
    // to avoid reloading on unrelated recompositions
    var loadedAccountId by remember { mutableStateOf("") }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) wv.goBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Single WebView — never destroyed, cookies preserved between switches
        AndroidView(
            factory = { context ->
                WebView(context).also { wv ->
                    webViewRef = wv
                    WebViewManager.configure(wv)

                    wv.webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                            isLoading = true
                            hasError = false
                        }
                        override fun onPageFinished(view: WebView, url: String) {
                            isLoading = false
                        }
                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            if (request.isForMainFrame) {
                                isLoading = false
                                hasError = true
                            }
                        }
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val url = request.url.toString()
                            return url.startsWith("intent:") || url.startsWith("market:")
                        }
                    }

                    wv.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView, newProgress: Int) {
                            loadProgress = newProgress / 100f
                            if (newProgress == 100) isLoading = false
                        }
                    }

                    // Initial load — no cookie clear
                    wv.loadUrl(account.url)
                    loadedAccountId = account.id
                }
            },
            update = { wv ->
                // Navigate only when account actually switches — compare by id, not url
                if (account.id != loadedAccountId) {
                    loadedAccountId = account.id
                    isLoading = true
                    hasError = false
                    wv.loadUrl(account.url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Thin progress bar just below status bar
        if (isLoading) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopStart),
                color = accentColor,
                trackColor = Color.Transparent
            )
        }

        // Floating pill — top center
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
                .wrapContentSize()
        ) {
            FloatingAccountPill(
                account = account,
                onClick = { dropdownExpanded = true }
            )
            AccountDropdownMenu(
                expanded = dropdownExpanded,
                accounts = accounts,
                activeAccountId = activeAccountId,
                onDismiss = { dropdownExpanded = false },
                onSelect = { id ->
                    dropdownExpanded = false
                    if (id != account.id) onSwitchAccount(id)
                },
                onDelete = { id ->
                    dropdownExpanded = false
                    onDeleteAccount(id)
                },
                onAddClick = {
                    dropdownExpanded = false
                    showAddDialog = true
                }
            )
        }

        // Error overlay
        if (hasError) {
            ErrorOverlay(
                accentColor = accentColor,
                onRetry = {
                    hasError = false
                    webViewRef?.reload()
                }
            )
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, platform, url ->
                onAddAccount(name, platform, url)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FloatingAccountPill(
    account: Account,
    onClick: () -> Unit
) {
    val accent = account.platform.accentColor()
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.82f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = accent.copy(alpha = 0.28f)
        ) {
            Text(
                text = account.platform.displayName(),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = account.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AccountDropdownMenu(
    expanded: Boolean,
    accounts: List<Account>,
    activeAccountId: String?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAddClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        accounts.forEach { acc ->
            val isActive = acc.id == activeAccountId
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = acc.platform.accentColor().copy(
                                alpha = if (isActive) 0.18f else 0.08f
                            )
                        ) {
                            Text(
                                text = acc.platform.displayName(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = acc.platform.accentColor()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = acc.name,
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) Color.Black else Color.DarkGray,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "●",
                                fontSize = 8.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = { onDelete(acc.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                onClick = { onSelect(acc.id) }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        DropdownMenuItem(
            text = {
                Text(
                    text = "+ Добавить аккаунт",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onAddClick
        )
    }
}

@Composable
private fun ErrorOverlay(
    accentColor: Color,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Ошибка подключения",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.padding(vertical = 6.dp))
            Text(
                text = "Проверьте интернет-соединение\nи попробуйте ещё раз.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(vertical = 14.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Повторить", color = Color.White)
            }
        }
    }
}
