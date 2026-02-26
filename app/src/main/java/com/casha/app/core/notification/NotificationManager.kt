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
class NotificationManager @Inject constructor(
    private val repository: com.casha.app.domain.repository.NotificationRepository
) {

    private val _notifications = MutableSharedFlow<NotificationCasha>(extraBufferCapacity = 1)
    val notifications = _notifications.asSharedFlow()

    val history = repository.getNotifications()
    val unreadCount = repository.getUnreadCount()

    /**
     * Dispatches a new notification received from FCM.
     */
    suspend fun dispatch(notification: NotificationCasha) {
        // Persist to local history
        repository.insertNotification(notification)
        
        // Emit for immediate UI feedback if needed
        _notifications.emit(notification)
    }

    suspend fun markAsRead(id: String) {
        repository.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        repository.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) {
        repository.deleteNotification(id)
    }

    suspend fun clearAll() {
        repository.deleteAllNotifications()
    }
}
