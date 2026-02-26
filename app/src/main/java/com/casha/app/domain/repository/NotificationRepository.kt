package com.casha.app.domain.repository

import com.casha.app.domain.model.NotificationCasha
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    // Remote
    suspend fun registerToken(token: String): Boolean
    suspend fun unregisterToken(token: String): Boolean

    // Local
    fun getNotifications(): Flow<List<NotificationCasha>>
    suspend fun insertNotification(notification: NotificationCasha)
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun deleteAllNotifications()
    fun getUnreadCount(): Flow<Int>
}
