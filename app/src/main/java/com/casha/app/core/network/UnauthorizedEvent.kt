package com.casha.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global event emitter for 401 Unauthorized responses.
 * Equivalent to iOS `NotificationCenter.default.post(name: .unauthorizedError)`.
 *
 * Observe this in your root Activity/ViewModel to trigger auto-logout:
 * ```kotlin
 * lifecycleScope.launch {
 *     UnauthorizedEvent.events.collect {
 *         authManager.clearAll()
 *         navController.navigate(NavRoutes.Login.route) { popUpTo(0) }
 *     }
 * }
 * ```
 */
object UnauthorizedEvent {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun emit() {
        _events.tryEmit(Unit)
    }
}
