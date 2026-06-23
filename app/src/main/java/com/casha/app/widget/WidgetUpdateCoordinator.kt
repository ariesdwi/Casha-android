package com.casha.app.widget

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Central coordinator for widget update events.
 * Provides an event-driven architecture for real-time widget updates.
 *
 * Usage:
 * ```
 * // Emit an event after a transaction is added
 * WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.TransactionAdded)
 * ```
 */
object WidgetUpdateCoordinator {
    
    private val _updateEvents = MutableSharedFlow<WidgetUpdateEvent>(
        replay = 0,
        extraBufferCapacity = 10
    )
    
    /**
     * Observable flow of widget update events.
     * Subscribe to this in the Application class to handle updates.
     */
    val updateEvents: SharedFlow<WidgetUpdateEvent> = _updateEvents.asSharedFlow()
    
    /**
     * Emit a widget update event.
     * This will trigger selective widget updates based on the event type.
     *
     * @param event The type of update event
     */
    fun emitUpdate(event: WidgetUpdateEvent) {
        _updateEvents.tryEmit(event)
    }
    
    /**
     * Widget update event types.
     * Each event type triggers specific widget updates to optimize performance.
     */
    sealed class WidgetUpdateEvent {
        /**
         * Transaction was added, updated, or deleted.
         * Updates: Budget widgets (shows spent today, safe spend)
         */
        object TransactionAdded : WidgetUpdateEvent()
        
        /**
         * Budget configuration changed (limits, categories, etc).
         * Updates: Budget widgets
         */
        object BudgetChanged : WidgetUpdateEvent()
        
        /**
         * Wallet balance was updated.
         * Updates: Balance widgets (currently lock screen circular balance)
         */
        object BalanceUpdated : WidgetUpdateEvent()
        
        /**
         * User logged in or logged out.
         * Updates: All widgets (state changes to LOGGED_OUT or NORMAL)
         */
        object LoginStateChanged : WidgetUpdateEvent()
        
        /**
         * Premium subscription state changed.
         * Updates: All widgets (state changes to NOT_PREMIUM or NORMAL)
         */
        object PremiumStateChanged : WidgetUpdateEvent()
        
        /**
         * Hide balance preference toggled.
         * Updates: All widgets (state changes to HIDDEN or NORMAL)
         */
        object HideBalanceToggled : WidgetUpdateEvent()
        
        /**
         * Periodic background refresh completed.
         * Updates: All widgets
         */
        object PeriodicRefresh : WidgetUpdateEvent()
        
        /**
         * Manual refresh triggered by user (pull to refresh, etc).
         * Updates: All widgets, forced
         */
        object ManualRefresh : WidgetUpdateEvent()
    }
}
