package com.casha.app.widget.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * SharedPreferences-based storage for widget data.
 * Both the main app (writer) and widget (reader) access this.
 */
object WidgetPreferences {
    private const val PREFS_NAME = "casha_widget_prefs"
    private const val KEY_WIDGET_SUMMARY = "widgetSummary"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_IS_PREMIUM = "isPremium"
    private const val KEY_LAST_UPDATED_AT = "lastUpdatedAt" // Epoch millis

    private val json = Json { ignoreUnknownKeys = true }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Read ──

    fun getSummary(context: Context): WidgetSummary? {
        val raw = prefs(context).getString(KEY_WIDGET_SUMMARY, null) ?: return null
        return try {
            json.decodeFromString<WidgetSummary>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun isPremium(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_PREMIUM, false)
    
    /**
     * Get the timestamp of when widget data was last updated.
     * Used to determine data staleness in the worker.
     * 
     * @return Instant of last update, or null if never updated
     */
    fun getLastUpdatedAt(context: Context): Instant? {
        val epochMillis = prefs(context).getLong(KEY_LAST_UPDATED_AT, -1L)
        return if (epochMillis > 0) {
            Instant.ofEpochMilli(epochMillis)
        } else {
            null
        }
    }

    // ── Write (called from main app) ──

    fun saveSummary(context: Context, summary: WidgetSummary) {
        prefs(context).edit()
            .putString(KEY_WIDGET_SUMMARY, json.encodeToString(WidgetSummary.serializer(), summary))
            .putLong(KEY_LAST_UPDATED_AT, System.currentTimeMillis()) // Auto-update timestamp
            .apply()
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun setPremium(context: Context, premium: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
    }
    
    /**
     * Manually update the last updated timestamp.
     * Usually called automatically by saveSummary(), but can be called explicitly if needed.
     */
    fun updateTimestamp(context: Context) {
        prefs(context).edit()
            .putLong(KEY_LAST_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }
}
