package com.casha.app.widget

import android.content.Context
import com.casha.app.widget.data.WidgetPreferences
import com.casha.app.widget.data.WidgetSummary
import com.casha.app.widget.worker.WidgetRefreshWorker

/**
 * Central utility for the main app to update widget data and trigger refreshes.
 */
object WidgetUpdater {

    /**
     * Save widget summary data and trigger an immediate widget refresh.
     * Call after fetching safe-spend data from API.
     */
    fun updateSummary(context: Context, summary: WidgetSummary) {
        WidgetPreferences.saveSummary(context, summary)
        WidgetRefreshWorker.refreshNow(context)
    }

    /**
     * Update auth state for widgets. Call on login/logout.
     */
    fun setAuthState(context: Context, isLoggedIn: Boolean, isPremium: Boolean) {
        WidgetPreferences.setLoggedIn(context, isLoggedIn)
        WidgetPreferences.setPremium(context, isPremium)
        WidgetRefreshWorker.refreshNow(context)
    }

    /**
     * Trigger a widget refresh (e.g., after saving a transaction).
     */
    fun refresh(context: Context) {
        WidgetRefreshWorker.refreshNow(context)
    }

    /**
     * Start periodic widget refresh schedule.
     * Call from Application.onCreate().
     */
    fun startPeriodicRefresh(context: Context) {
        WidgetRefreshWorker.enqueue(context)
    }

    /**
     * Stop periodic refresh (on logout).
     */
    fun stopPeriodicRefresh(context: Context) {
        WidgetRefreshWorker.cancel(context)
    }
}
