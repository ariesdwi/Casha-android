package com.casha.app.core.notification

import com.casha.app.domain.model.NotificationCasha
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages in-app notification events and navigation flow.
 * Mirroring iOS NotificationHandler logic.
 */
@Singleton
class NotificationManager @Inject constructor() {

    private val _notifications = MutableSharedFlow<NotificationCasha>(extraBufferCapacity = 1)
    val notifications = _notifications.asSharedFlow()

    /**
     * Dispatches a new notification received from FCM.
     */
    suspend fun dispatch(notification: NotificationCasha) {
        _notifications.emit(notification)
        // Note: Local persistence could be added here if needed, 
        // mirroring iOS local storage logic.
    }
}
