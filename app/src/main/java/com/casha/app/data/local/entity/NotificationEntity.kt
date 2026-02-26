package com.casha.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.casha.app.domain.model.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val imageUrl: String?,
    val data: Map<String, String>,
    val createdAt: Long,
    val isRead: Boolean = false
)
