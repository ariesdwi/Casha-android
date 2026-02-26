package com.casha.app.data.remote.impl

import com.casha.app.data.local.dao.NotificationDao
import com.casha.app.data.remote.api.NotificationApiService
import com.casha.app.data.remote.dto.RegisterTokenRequestDTO
import com.casha.app.data.remote.dto.toDomain
import com.casha.app.data.remote.dto.toEntity
import com.casha.app.domain.model.NotificationCasha
import com.casha.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.casha.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: NotificationApiService,
    private val dao: NotificationDao
) : NotificationRepository {

    override suspend fun registerToken(token: String): Boolean {
        val result = safeApiCall { apiService.registerToken(RegisterTokenRequestDTO(token)) }
        return result.getOrNull()?.data?.success == true
    }

    override suspend fun unregisterToken(token: String): Boolean {
        val result = safeApiCall { apiService.unregisterToken(token) }
        return result.getOrNull()?.data?.success == true
    }

    override fun getNotifications(): Flow<List<NotificationCasha>> {
        return dao.getAllNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertNotification(notification: NotificationCasha) {
        dao.insertNotification(notification.toEntity())
    }

    override suspend fun markAsRead(id: String) {
        dao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String) {
        dao.deleteNotification(id)
    }

    override suspend fun deleteAllNotifications() {
        dao.deleteAllNotifications()
    }

    override fun getUnreadCount(): Flow<Int> {
        return dao.getUnreadCount()
    }
}
