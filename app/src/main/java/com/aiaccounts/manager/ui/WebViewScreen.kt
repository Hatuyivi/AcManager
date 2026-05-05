package com.aiaccounts.manager.ui

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aiaccounts.manager.model.Account
import com.aiaccounts.manager.webview.WebViewManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    account: Account,
    onBack: () -> Unit,
    onMessageSent: () -> Unit
) {
    val accentColor = account.platform.accentColor()

    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var pageTitle by remember { mutableStateOf(account.name) }
    var hasError by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = pageTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            val wv = webViewRef
                            if (wv != null && wv.canGoBack()) {
                                wv.goBack()
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { loadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = accentColor,
                        trackColor = Color.Black.copy(alpha = 0.1f)
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasError) {
                ErrorView(
                    accentColor = accentColor,
                    onRetry = {
                        hasError = false
                        webViewRef?.reload()
                    }
                )
            }

            AndroidView(
                factory = { context ->
                    WebView(context).also { webView ->
                        webViewRef = webView
                        WebViewManager.configure(webView)

                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                isLoading = true
                                hasError = false
                            }

                            override fun onPageFinished(view: WebView, url: String) {
                                isLoading = false
                                pageTitle = view.title ?: account.name
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

                        webView.webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                loadProgress = newProgress / 100f
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                            }

                            override fun onReceivedTitle(view: WebView, title: String) {
                                pageTitle = title.ifBlank { account.name }
                            }
                        }

                        webView.loadUrl(account.url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ErrorView(
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
                text = "Connection error",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Check your internet connection and try again.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(vertical = 16.dp)
            )
            androidx.compose.material3.Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}
