package com.casha.app.widget.util

import android.util.Log
import com.casha.app.widget.WidgetUpdateCoordinator
import java.time.Duration
import java.time.Instant

/**
 * Centralized logging utility for widget operations.
 * Provides structured logging for debugging and monitoring.
 */
object WidgetLogger {
    
    private const val TAG = "CashaWidget"
    
    /**
     * Enable or disable debug logging.
     * Set to false in production builds.
     */
    var debugEnabled: Boolean = true // TODO: Set based on BuildConfig.DEBUG
    
    // ========== Update Events ==========
    
    /**
     * Log the start of a widget update operation.
     */
    fun logUpdateStart(event: WidgetUpdateCoordinator.WidgetUpdateEvent) {
        if (debugEnabled) {
            Log.d(TAG, "Widget update started: ${event.javaClass.simpleName}")
        }
    }
    
    /**
     * Log successful widget update.
     */
    fun logUpdateSuccess(event: WidgetUpdateCoordinator.WidgetUpdateEvent) {
        if (debugEnabled) {
            Log.d(TAG, "Widget update completed: ${event.javaClass.simpleName}")
        }
    }
    
    /**
     * Log widget update error.
     */
    fun logUpdateError(event: WidgetUpdateCoordinator.WidgetUpdateEvent, error: Throwable) {
        Log.e(TAG, "Widget update failed: ${event.javaClass.simpleName}", error)
    }
    
    // ========== Individual Widget Updates ==========
    
    /**
     * Log individual widget type update.
     */
    fun logWidgetUpdate(
        widgetType: String,
        count: Int,
        success: Boolean,
        error: Throwable? = null
    ) {
        if (success) {
            if (debugEnabled) {
                Log.d(TAG, "Updated $widgetType: $count instances")
            }
        } else {
            Log.e(TAG, "Failed to update $widgetType: $count instances", error)
        }
    }
    
    // ========== Data Fetch ==========
    
    /**
     * Log widget data fetch operation.
     */
    fun logDataFetch(
        success: Boolean,
        dataAge: Duration? = null,
        error: Throwable? = null
    ) {
        if (success) {
            val ageMinutes = dataAge?.toMinutes() ?: 0
            if (debugEnabled) {
                Log.d(TAG, "Widget data fetched successfully (age: ${ageMinutes}min)")
            }
        } else {
            Log.e(TAG, "Widget data fetch failed", error)
        }
    }
    
    /**
     * Log when stale data is detected.
     */
    fun logStaleData(lastUpdate: Instant?, currentTime: Instant) {
        if (debugEnabled) {
            if (lastUpdate != null) {
                val age = Duration.between(lastUpdate, currentTime).toMinutes()
                Log.d(TAG, "Widget data is stale (age: ${age}min)")
            } else {
                Log.d(TAG, "Widget data has never been updated")
            }
        }
    }
    
    /**
     * Log when data is fresh and update is skipped.
     */
    fun logFreshData(lastUpdate: Instant, currentTime: Instant) {
        if (debugEnabled) {
            val age = Duration.between(lastUpdate, currentTime).toMinutes()
            Log.d(TAG, "Widget data is fresh (age: ${age}min), skipping update")
        }
    }
    
    // ========== Worker Operations ==========
    
    /**
     * Log worker start.
     */
    fun logWorkerStart(workerName: String) {
        if (debugEnabled) {
            Log.d(TAG, "Worker started: $workerName")
        }
    }
    
    /**
     * Log worker completion.
     */
    fun logWorkerSuccess(workerName: String) {
        if (debugEnabled) {
            Log.d(TAG, "Worker completed: $workerName")
        }
    }
    
    /**
     * Log worker failure.
     */
    fun logWorkerError(workerName: String, error: Throwable) {
        Log.e(TAG, "Worker failed: $workerName", error)
    }
    
    /**
     * Log worker retry.
     */
    fun logWorkerRetry(workerName: String, attempt: Int, maxAttempts: Int) {
        Log.w(TAG, "Worker retry: $workerName (attempt $attempt/$maxAttempts)")
    }
    
    /**
     * Log when worker skips unnecessary work.
     */
    fun logWorkerSkipped(workerName: String, reason: String) {
        if (debugEnabled) {
            Log.d(TAG, "Worker skipped: $workerName (reason: $reason)")
        }
    }
    
    // ========== Network Status ==========
    
    /**
     * Log network availability check.
     */
    fun logNetworkStatus(available: Boolean) {
        if (debugEnabled) {
            val status = if (available) "available" else "unavailable"
            Log.d(TAG, "Network is $status")
        }
    }
    
    /**
     * Log network-related failure.
     */
    fun logNetworkError(error: Throwable) {
        Log.e(TAG, "Network error during widget operation", error)
    }
    
    // ========== Render Events ==========
    
    /**
     * Log widget render with state information.
     */
    fun logRender(widgetType: String, state: String) {
        if (debugEnabled) {
            Log.d(TAG, "Rendering $widgetType in state: $state")
        }
    }
    
    /**
     * Log render error.
     */
    fun logRenderError(widgetType: String, error: Throwable) {
        Log.e(TAG, "Render error in $widgetType", error)
    }
    
    // ========== Performance Metrics ==========
    
    /**
     * Log performance timing for an operation.
     */
    fun logPerformance(operation: String, durationMs: Long) {
        if (debugEnabled) {
            Log.d(TAG, "Performance: $operation took ${durationMs}ms")
        }
    }
    
    /**
     * Log slow operation warning.
     */
    fun logSlowOperation(operation: String, durationMs: Long, thresholdMs: Long) {
        Log.w(TAG, "Slow operation: $operation took ${durationMs}ms (threshold: ${thresholdMs}ms)")
    }
}
