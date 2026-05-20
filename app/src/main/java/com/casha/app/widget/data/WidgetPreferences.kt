package com.casha.app.widget.data

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * SharedPreferences-based storage for widget data.
 * Both the main app (writer) and widget (reader) access this.
 */
object WidgetPreferences {
    private const val PREFS_NAME = "casha_widget_prefs"
    private const val KEY_WIDGET_SUMMARY = "widgetSummary"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_IS_PREMIUM = "isPremium"

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

    // ── Write (called from main app) ──

    fun saveSummary(context: Context, summary: WidgetSummary) {
        prefs(context).edit()
            .putString(KEY_WIDGET_SUMMARY, json.encodeToString(WidgetSummary.serializer(), summary))
            .apply()
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun setPremium(context: Context, premium: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
    }
}
