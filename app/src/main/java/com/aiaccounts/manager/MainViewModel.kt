package com.aiaccounts.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aiaccounts.manager.data.AccountRepository
import com.aiaccounts.manager.model.Account
import com.aiaccounts.manager.model.Platform
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(private val repository: AccountRepository) : ViewModel() {

    val accounts: StateFlow<List<Account>> = repository.accounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val activeAccountId: StateFlow<String?> = repository.activeAccountId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun addAccount(name: String, platform: Platform, url: String) {
        if (name.isBlank() || url.isBlank()) return
        val account = Account(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            platform = platform,
            url = url.trim()
        )
        viewModelScope.launch {
            repository.addAccount(account)
        }
    }

    fun selectAccount(id: String) {
        viewModelScope.launch {
            repository.setActiveAccount(id)
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun incrementMessageCount(id: String) {
        viewModelScope.launch {
            repository.incrementMessageCount(id)
        }
    }

    class Factory(private val repository: AccountRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
