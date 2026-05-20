package com.casha.app.widget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.casha.app.widget.ui.HomeSmallWidget
import com.casha.app.widget.ui.HomeMediumWidget
import com.casha.app.widget.ui.LockCircularWidget
import com.casha.app.widget.ui.LockRectangularWidget
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager worker that refreshes all Casha widgets.
 * Scheduled every 15 minutes.
 */
@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Update all widget instances
        HomeSmallWidget().updateAll(applicationContext)
        HomeMediumWidget().updateAll(applicationContext)
        LockCircularWidget().updateAll(applicationContext)
        LockRectangularWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "casha_widget_refresh"

        /**
         * Enqueue periodic widget refresh (every 15 min).
         * Called from Application.onCreate() or after login.
         */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * Trigger an immediate one-shot refresh (after transaction save, etc.)
         */
        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        /**
         * Cancel periodic refresh (on logout).
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
