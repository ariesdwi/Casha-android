package com.casha.app.core.network

import com.casha.app.core.config.AppConfig

/**
 * Wraps API calls with structured error handling.
 *
 * Usage:
 * ```kotlin
 * val result = safeApiCall { apiService.login(request) }
 * result.fold(
 *     onSuccess = { response -> /* handle success */ },
 *     onFailure = { error -> /* handle NetworkError */ }
 * )
 * ```
 */
suspend fun <T> safeApiCall(
    call: suspend () -> T
): Result<T> {
    return try {
        Result.success(call())
    } catch (e: NetworkError) {
        AppConfig.log("🔴 API Error: ${e.message}", AppConfig.LogLevel.ERROR)
        Result.failure(e)
    } catch (e: retrofit2.HttpException) {
        val statusCode = e.code()
        val message = e.message() ?: "HTTP Error $statusCode"
        AppConfig.log("🔴 HTTP $statusCode: $message", AppConfig.LogLevel.ERROR)
        Result.failure(
            when (statusCode) {
                401 -> NetworkError.Unauthorized()
                408 -> NetworkError.Timeout()
                else -> NetworkError.ServerError(message)
            }
        )
    } catch (e: kotlinx.serialization.SerializationException) {
        AppConfig.log("🔴 Decoding Error: ${e.message}", AppConfig.LogLevel.ERROR)
        Result.failure(NetworkError.DecodingFailed(e))
    } catch (e: java.net.SocketTimeoutException) {
        Result.failure(NetworkError.Timeout())
    } catch (e: java.io.IOException) {
        Result.failure(NetworkError.NoConnection())
    } catch (e: Exception) {
        AppConfig.log("🔴 Unknown Error: ${e.message}", AppConfig.LogLevel.ERROR)
        Result.failure(NetworkError.Unknown(e))
    }
}
