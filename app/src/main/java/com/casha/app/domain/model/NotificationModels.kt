package com.casha.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Types of push notifications supported by the system.
 * Mirroring iOS NotificationType.swift.
 */
enum class NotificationType {
    TRANSACTION_ADDED,
    TRANSACTION_REMINDER,
    BUDGET_ALERT,
    BUDGET_CREATED,
    WELCOME,
    MONTHLY_SUMMARY,
    UNKNOWN
}

/**
 * Domain model for push notification data.
 */
@Serializable
data class NotificationCasha(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val imageUrl: String? = null,
    val data: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
