package com.aiaccounts.manager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

fun generateNameFromLogin(input: String): String {
    val local = if (input.contains("@")) input.substringBefore("@") else input
    return local
        .split(".", "_", "-", "+")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .ifBlank { input.ifBlank { "Account" } }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, platform: Platform, url: String) -> Unit
) {
    var login by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://claude.ai") }
    var platform by remember { mutableStateOf(Platform.CLAUDE) }
    var platformMenuExpanded by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    val generatedName = generateNameFromLogin(login)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить аккаунт", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it; loginError = false },
                    label = { Text("Логин или email") },
                    placeholder = { Text("example@gmail.com") },
                    isError = loginError,
                    supportingText = if (loginError) {
                        { Text("Введите логин или email") }
                    } else if (login.isNotBlank()) {
                        { Text("Имя: $generatedName", color = Color.Gray, fontSize = 12.sp) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedButton(
                        onClick = { platformMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Платформа: ${platform.displayName()}",
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
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    loginError = login.isBlank()
                    if (!loginError) onConfirm(generatedName, platform, url)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.Gray)
            }
        }
    )
}
