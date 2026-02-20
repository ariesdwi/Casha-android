package com.casha.app.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "casha_prefs")

/**
 * Manages authentication tokens and user preferences using DataStore.
 */
@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_CURRENCY = stringPreferencesKey("selected_currency")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }

    // ── Token ──

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
        }
    }

    // ── Currency ──

    val selectedCurrency: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY]
    }

    suspend fun saveCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENCY] = currency
        }
    }

    // ── User ID ──

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
        }
    }

    // ── Logout ──

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
