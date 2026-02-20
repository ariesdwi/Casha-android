package com.casha.app.core.network

import java.io.IOException

/**
 * Encapsulates the error format returned by the Casha API.
 */
data class APIErrorResponse(
    val message: String,
    val error: String? = null,
    val statusCode: Int? = null
)

/**
 * Sealed class representing network errors — aligned with iOS `NetworkError`.
 */
sealed class NetworkError : IOException() {

    object InvalidURL : NetworkError() {
        override val message: String = "Invalid URL"
    }

    object InvalidRequest : NetworkError() {
        override val message: String = "Invalid Request"
    }

    data class RequestFailed(
        override val message: String
    ) : NetworkError()

    data class InvalidResponse(
        val statusCode: Int
    ) : NetworkError() {
        override val message: String = "Invalid Response: $statusCode"
    }

    data class ServerError(
        override val message: String
    ) : NetworkError()

    data class DecodingFailed(
        val underlyingError: Exception
    ) : NetworkError() {
        override val message: String = "Failed to parse response: ${underlyingError.message}"
    }

    data class Unknown(
        val underlyingError: Exception
    ) : NetworkError() {
        override val message: String = underlyingError.message ?: "Something went wrong"
    }
    
    // Kept for backward compatibility with existing ErrorInterceptor / Authentication logic
    data class Unauthorized(
        override val message: String = "Session expired. Please login again."
    ) : NetworkError()
    
    data class NoConnection(
        override val message: String = "No internet connection"
    ) : NetworkError()
    
    data class Timeout(
        override val message: String = "Request timed out"
    ) : NetworkError()
}
