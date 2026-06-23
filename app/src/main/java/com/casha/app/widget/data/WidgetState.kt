package com.casha.app.widget.data

import android.content.Context

/**
 * Determines which state a widget should display.
 */
enum class WidgetState {
    NORMAL,
    LOGGED_OUT,
    NO_DATA,
    NOT_PREMIUM,
    HIDDEN
}

fun resolveWidgetState(context: Context): Pair<WidgetState, WidgetSummary?> {
    if (!WidgetPreferences.isLoggedIn(context)) {
        return WidgetState.LOGGED_OUT to null
    }
    if (!WidgetPreferences.isPremium(context)) {
        return WidgetState.NOT_PREMIUM to null
    }
    val summary = WidgetPreferences.getSummary(context)
    if (summary == null || summary.lastUpdatedAt == null) {
        return WidgetState.NO_DATA to null
    }
    if (summary.hideBalance) {
        return WidgetState.HIDDEN to summary
    }
    return WidgetState.NORMAL to summary
}
