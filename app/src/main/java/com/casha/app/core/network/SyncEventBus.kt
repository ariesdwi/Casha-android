package com.casha.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A global event bus to coordinate data refreshes across different ViewModels.
 * Used to emulate SwiftUI's `@EnvironmentObject` or single-source-of-truth reactivity.
 */
@Singleton
class SyncEventBus @Inject constructor() {
    private val _syncCompletedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    
    /**
     * Flow that emits an event whenever data is modified and other screens should refresh.
     */
    val syncCompletedEvent = _syncCompletedEvent.asSharedFlow()

    /**
     * Call this when a significant data mutation has occurred (e.g. saving/deleting a transaction)
     * so that the Dashboard or other observers can refresh.
     */
    fun emitSyncCompleted() {
        _syncCompletedEvent.tryEmit(Unit)
    }
}
