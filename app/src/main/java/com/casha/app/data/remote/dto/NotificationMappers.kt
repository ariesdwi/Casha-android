package com.casha.app.data.remote.dto

import com.casha.app.data.local.entity.NotificationEntity
import com.casha.app.domain.model.NotificationCasha
import com.casha.app.domain.model.NotificationType

fun NotificationEntity.toDomain(): NotificationCasha {
    return NotificationCasha(
        id = id,
        title = title,
        body = body,
        type = type,
        imageUrl = imageUrl,
        data = data,
        createdAt = createdAt,
        isRead = isRead
    )
}

fun NotificationCasha.toEntity(isRead: Boolean = false): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        body = body,
        type = type,
        imageUrl = imageUrl,
        data = data,
        createdAt = createdAt,
        isRead = isRead
    )
}
