package com.casha.app.core.network

import kotlinx.serialization.Serializable

/**
 * Result wrapper for network calls.
 * Similar to custom iOS Error types.
 */
sealed class NetworkError : Exception() {
    data class NoConnection(override val message: String = "No internet connection") : NetworkError()
    data class Timeout(override val message: String = "Request timed out") : NetworkError()
    data class Unauthorized(override val message: String = "Session expired") : NetworkError()
    data class ServerError(override val message: String = "Server internal error") : NetworkError()
    data class ApiError(val code: Int, override val message: String) : NetworkError()
    data class RequestFailed(override val message: String) : NetworkError()
    data class Unknown(val original: Throwable) : NetworkError() {
        override val message: String? = original.message
    }
}

@Serializable
data class ApiError(
    val status: String? = null,
    val message: String? = null
)
