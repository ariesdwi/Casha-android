package com.casha.app.data.remote.dto

import com.casha.app.domain.model.LoginResult
import com.casha.app.domain.model.UserCasha
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

// ─────────────────────────────────────────────
// Request DTOs
// ─────────────────────────────────────────────

@Serializable
data class LoginRequestDTO(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestDTO(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class GoogleLoginRequestDTO(
    @SerialName("id_token")
    val idToken: String
)

@Serializable
data class ResetPasswordRequestDTO(
    val email: String
)

@Serializable
data class UpdateProfileRequestDTO(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val currency: String? = null
)

// ─────────────────────────────────────────────
// Response DTOs
// ─────────────────────────────────────────────

@Serializable
data class LoginResponseDTO(
    @SerialName("access_token")
    val accessToken: String,
    val currency: String? = null
)

@Serializable
data class RegisterResponseDTO(
    @SerialName("access_token")
    val accessToken: String
)

@Serializable
data class ProfileDTO(
    @SerialName("_id")
    val id: String,
    val email: String,
    val name: String,
    val avatar: String? = null,
    val phone: String? = null,
    val currency: String = "USD",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// ─────────────────────────────────────────────
// Mappers
// ─────────────────────────────────────────────

fun LoginResponseDTO.toLoginResult(): LoginResult {
    return LoginResult(
        token = accessToken,
        currency = currency
    )
}

fun ProfileDTO.toDomain(): UserCasha {
    return UserCasha(
        id = id,
        email = email,
        name = name,
        avatar = avatar,
        phone = phone,
        currency = currency,
        createdAt = parseDate(createdAt),
        updatedAt = parseDate(updatedAt)
    )
}

fun com.casha.app.domain.model.UpdateProfileRequest.toDTO(): UpdateProfileRequestDTO {
    return UpdateProfileRequestDTO(
        name = name,
        email = email,
        phone = phone,
        avatar = avatar,
        currency = currency
    )
}

private fun parseDate(dateString: String?): Date {
    if (dateString == null) return Date()
    return try {
        val instant = java.time.Instant.parse(dateString)
        Date.from(instant)
    } catch (e: Exception) {
        Date()
    }
}
