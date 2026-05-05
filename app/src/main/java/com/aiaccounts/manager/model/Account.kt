package com.aiaccounts.manager.model

import kotlinx.serialization.Serializable

@Serializable
enum class Platform {
    CLAUDE,
    GEMINI
}

@Serializable
data class Account(
    val id: String,
    val name: String,
    val platform: Platform,
    val url: String,
    val messageCount: Int = 0
)
