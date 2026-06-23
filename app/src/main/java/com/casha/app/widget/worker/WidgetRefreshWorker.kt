package com.casha.app.widget.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateDispatcher
import com.casha.app.widget.data.WidgetPreferences
import com.casha.app.widget.ui.HomeSmallWidget
import com.casha.app.widget.ui.HomeMediumWidget
import com.casha.app.widget.ui.LockCircularWidget
import com.casha.app.widget.ui.LockRectangularWidget
import com.casha.app.widget.util.WidgetLogger
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Smart periodic WorkManager worker that refreshes all Casha widgets.
 * 
 * Enhanced features:
 * - Network awareness (skip if offline)
 * - Staleness check (skip if data is fresh)
 * - Automatic retry with exponential backoff
 * - Battery-aware constraints
 * - Comprehensive logging
 * 
 * Scheduled every 15 minutes with intelligent skip logic to save battery.
 */
@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val workerName = "WidgetRefreshWorker"
        WidgetLogger.logWorkerStart(workerName)
        
        return try {
            // 1. Check network connectivity
            if (!isNetworkAvailable()) {
                WidgetLogger.logWorkerSkipped(workerName, "No network connectivity")
                WidgetLogger.logNetworkStatus(false)
                // Retry when network is available
                return Result.retry()
            }
            
            WidgetLogger.logNetworkStatus(true)
            
            // 2. Check if data is stale (last update > 15 min ago)
            val lastUpdate = WidgetPreferences.getLastUpdatedAt(applicationContext)
            val now = Instant.now()
            
            if (!isDataStale(lastUpdate, now)) {
                WidgetLogger.logFreshData(lastUpdate!!, now)
                WidgetLogger.logWorkerSkipped(workerName, "Data is still fresh")
                // Success but no work needed
                return Result.success()
            }
            
            if (lastUpdate != null) {
                WidgetLogger.logStaleData(lastUpdate, now)
            }
            
            // 3. Refresh widgets using the new dispatcher
            val dispatcher = WidgetUpdateDispatcher(applicationContext)
            dispatcher.updateWidgets(
                event = WidgetUpdateCoordinator.WidgetUpdateEvent.PeriodicRefresh,
                force = true
            )
            
            // 4. Also trigger Glance widget updates (for good measure)
            HomeSmallWidget().updateAll(applicationContext)
            HomeMediumWidget().updateAll(applicationContext)
            LockCircularWidget().updateAll(applicationContext)
            LockRectangularWidget().updateAll(applicationContext)
            
            WidgetLogger.logWorkerSuccess(workerName)
            Result.success()
        } catch (e: Exception) {
            WidgetLogger.logWorkerError(workerName, e)
            
            // Retry with exponential backoff
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                WidgetLogger.logWorkerRetry(workerName, runAttemptCount + 1, MAX_RETRY_ATTEMPTS)
                Result.retry()
            } else {
                // Max retries reached, give up
                Result.failure()
            }
        }
    }
    
    /**
     * Check if network is available.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as? ConnectivityManager ?: return false
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Check if widget data is stale (older than STALE_THRESHOLD).
     * 
     * @param lastUpdate Last update timestamp
     * @param currentTime Current timestamp
     * @return true if data needs refresh, false if still fresh
     */
    private fun isDataStale(lastUpdate: Instant?, currentTime: Instant): Boolean {
        // No data yet, definitely stale
        if (lastUpdate == null) return true
        
        // Calculate age
        val age = Duration.between(lastUpdate, currentTime)
        
        // Data is stale if older than threshold
        return age.toMinutes() >= STALE_THRESHOLD_MINUTES
    }

    companion object {
        private const val WORK_NAME = "casha_widget_refresh"
        private const val STALE_THRESHOLD_MINUTES = 15L
        private const val MAX_RETRY_ATTEMPTS = 3

        /**
         * Enqueue smart periodic widget refresh (every 15 min).
         * Called from Application.onCreate() or after login.
         * 
         * Features:
         * - Requires network connectivity
         * - Battery-aware (won't run when battery is low)
         * - Automatic retry with exponential backoff
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true) // Don't drain battery
                .build()
            
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("widget_refresh")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                request
            )
            
            WidgetLogger.logWorkerStart("enqueue periodic refresh")
        }

        /**
         * Trigger an immediate one-shot refresh.
         * Use after transaction save, budget update, etc.
         * 
         * Note: This is now mostly replaced by the event-driven WidgetUpdateDispatcher,
         * but kept for backward compatibility.
         */
        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("widget_refresh_now")
                .build()
                
            WorkManager.getInstance(context).enqueue(request)
            WidgetLogger.logWorkerStart("immediate refresh")
        }

        /**
         * Cancel periodic refresh (on logout).
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WidgetLogger.logWorkerStart("cancel periodic refresh")
        }
    }
}
