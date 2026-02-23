package com.casha.app.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.subscriptionDataStore: DataStore<Preferences> by preferencesDataStore(name = "subscription_prefs")

@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    val isPremium: Flow<Boolean> = context.subscriptionDataStore.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.subscriptionDataStore.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = isPremium
        }
    }
}
