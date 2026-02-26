package com.casha.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterTokenRequestDTO(
    val token: String,
    val platform: String = "android"
)

@Serializable
data class PushNotificationResponseDto(
    val success: Boolean,
    val message: String? = null
)
