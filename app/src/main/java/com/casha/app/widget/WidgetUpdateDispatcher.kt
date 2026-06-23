package com.casha.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.casha.app.widget.ui.*
import com.casha.app.widget.util.WidgetLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Smart widget update dispatcher that selectively updates widgets based on event type.
 * Replaces the old WidgetUpdater with more efficient, targeted updates.
 *
 * Benefits:
 * - Battery efficient (only updates relevant widgets)
 * - Performance optimized (selective updates)
 * - Logging and diagnostics
 * - Error handling
 */
class WidgetUpdateDispatcher(private val context: Context) {
    
    private val appWidgetManager = AppWidgetManager.getInstance(context)
    
    /**
     * Update widgets based on the event type.
     * 
     * @param event The update event that triggered this call
     * @param force If true, update all widgets regardless of event type
     */
    suspend fun updateWidgets(
        event: WidgetUpdateCoordinator.WidgetUpdateEvent,
        force: Boolean = false
    ) = withContext(Dispatchers.IO) {
        WidgetLogger.logUpdateStart(event)
        
        try {
            when {
                force -> updateAllWidgets()
                else -> when (event) {
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.TransactionAdded -> {
                        updateBudgetWidgets()
                    }
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.BudgetChanged -> {
                        updateBudgetWidgets()
                    }
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.BalanceUpdated -> {
                        updateBalanceWidgets()
                    }
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.LoginStateChanged,
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.PremiumStateChanged,
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.HideBalanceToggled,
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.PeriodicRefresh,
                    is WidgetUpdateCoordinator.WidgetUpdateEvent.ManualRefresh -> {
                        updateAllWidgets()
                    }
                }
            }
            
            WidgetLogger.logUpdateSuccess(event)
        } catch (e: Exception) {
            WidgetLogger.logUpdateError(event, e)
        }
    }
    
    /**
     * Update only budget-related widgets (HomeSmall, HomeMedium, Lock widgets).
     */
    private fun updateBudgetWidgets() {
        updateWidget(HomeSmallWidgetReceiver::class.java)
        updateWidget(HomeMediumWidgetReceiver::class.java)
        updateWidget(LockCircularWidgetReceiver::class.java)
        updateWidget(LockRectangularWidgetReceiver::class.java)
    }
    
    /**
     * Update only balance-related widgets.
     * Currently this is the lock screen circular widget showing balance ring.
     */
    private fun updateBalanceWidgets() {
        // Future: Add balance-specific widgets here
        updateWidget(LockCircularWidgetReceiver::class.java)
    }
    
    /**
     * Update all widgets.
     */
    private fun updateAllWidgets() {
        updateWidget(HomeSmallWidgetReceiver::class.java)
        updateWidget(HomeMediumWidgetReceiver::class.java)
        updateWidget(LockCircularWidgetReceiver::class.java)
        updateWidget(LockRectangularWidgetReceiver::class.java)
    }
    
    /**
     * Update a specific widget type.
     */
    private fun updateWidget(receiverClass: Class<*>) {
        try {
            val componentName = ComponentName(context, receiverClass)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            if (widgetIds.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(
                    widgetIds,
                    android.R.id.background
                )
                
                WidgetLogger.logWidgetUpdate(
                    widgetType = receiverClass.simpleName,
                    count = widgetIds.size,
                    success = true
                )
            }
        } catch (e: Exception) {
            WidgetLogger.logWidgetUpdate(
                widgetType = receiverClass.simpleName,
                count = 0,
                success = false,
                error = e
            )
        }
    }
}
