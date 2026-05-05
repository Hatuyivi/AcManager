package com.aiaccounts.manager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aiaccounts.manager.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "accounts")

class AccountRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val ACCOUNTS = stringPreferencesKey("accounts_json")
        val ACTIVE_ACCOUNT_ID = stringPreferencesKey("active_account_id")
    }

    val accounts: Flow<List<Account>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.ACCOUNTS] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<Account>>(raw) }.getOrElse { emptyList() }
    }

    val activeAccountId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_ACCOUNT_ID]
    }

    suspend fun addAccount(account: Account) {
        context.dataStore.edit { prefs ->
            val current = parseAccounts(prefs)
            val updated = current + account
            prefs[Keys.ACCOUNTS] = json.encodeToString(updated)
        }
    }

    suspend fun deleteAccount(id: String) {
        context.dataStore.edit { prefs ->
            val current = parseAccounts(prefs)
            val updated = current.filter { it.id != id }
            prefs[Keys.ACCOUNTS] = json.encodeToString(updated)
            if (prefs[Keys.ACTIVE_ACCOUNT_ID] == id) {
                prefs.remove(Keys.ACTIVE_ACCOUNT_ID)
            }
        }
    }

    suspend fun setActiveAccount(id: String?) {
        context.dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(Keys.ACTIVE_ACCOUNT_ID)
            } else {
                prefs[Keys.ACTIVE_ACCOUNT_ID] = id
            }
        }
    }

    suspend fun incrementMessageCount(id: String) {
        context.dataStore.edit { prefs ->
            val current = parseAccounts(prefs)
            val updated = current.map { account ->
                if (account.id == id) account.copy(messageCount = account.messageCount + 1)
                else account
            }
            prefs[Keys.ACCOUNTS] = json.encodeToString(updated)
        }
    }

    private fun parseAccounts(prefs: Preferences): List<Account> {
        val raw = prefs[Keys.ACCOUNTS] ?: return emptyList()
        return runCatching { json.decodeFromString<List<Account>>(raw) }.getOrElse { emptyList() }
    }
}
