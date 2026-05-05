package com.aiaccounts.manager.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiaccounts.manager.model.Account
import com.aiaccounts.manager.model.Platform

val ClaudeColor = Color(0xFFE8793A)
val GeminiColor = Color(0xFF4285F4)

fun Platform.accentColor(): Color = when (this) {
    Platform.CLAUDE -> ClaudeColor
    Platform.GEMINI -> GeminiColor
}

fun Platform.displayName(): String = when (this) {
    Platform.CLAUDE -> "Claude"
    Platform.GEMINI -> "Gemini"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    accounts: List<Account>,
    activeAccountId: String?,
    onSelectAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onAddAccount: (name: String, platform: Platform, url: String) -> Unit,
    onOpenWebView: (accountId: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Accounts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add account")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (accounts.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(accounts, key = { it.id }) { account ->
                    AccountCard(
                        account = account,
                        isActive = account.id == activeAccountId,
                        onSelect = { onSelectAccount(account.id) },
                        onDelete = { onDeleteAccount(account.id) },
                        onOpen = { onOpenWebView(account.id) }
                    )
                }
            }
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
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No accounts yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to add your first AI account",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    val accent = account.platform.accentColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isActive) BorderStroke(2.dp, accent) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformBadge(platform = account.platform)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = account.url,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                StatusBadge(isActive = isActive)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${account.messageCount} messages",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isActive) {
                        OutlinedButton(
                            onClick = onSelect,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Black),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activate", fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Open", fontSize = 12.sp, color = Color.White)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformBadge(platform: Platform) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = platform.accentColor().copy(alpha = 0.12f)
    ) {
        Text(
            text = platform.displayName(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = platform.accentColor()
        )
    }
}

@Composable
private fun StatusBadge(isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) Color(0xFF2E7D32) else Color.Gray
        )
    }
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, platform: Platform, url: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf(Platform.CLAUDE) }
    var platformMenuExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Account name") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedButton(
                        onClick = { platformMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Platform: ${platform.displayName()}",
                            color = platform.accentColor(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    DropdownMenu(
                        expanded = platformMenuExpanded,
                        onDismissRequest = { platformMenuExpanded = false }
                    ) {
                        Platform.entries.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = p.displayName(),
                                        color = p.accentColor(),
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    platform = p
                                    url = when (p) {
                                        Platform.CLAUDE -> "https://claude.ai"
                                        Platform.GEMINI -> "https://gemini.google.com"
                                    }
                                    platformMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = false
                    },
                    label = { Text("URL") },
                    isError = urlError,
                    supportingText = if (urlError) {
                        { Text("URL is required") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    urlError = url.isBlank()
                    if (!nameError && !urlError) {
                        onConfirm(name, platform, url)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
