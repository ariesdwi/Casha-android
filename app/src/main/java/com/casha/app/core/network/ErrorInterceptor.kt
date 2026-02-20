package com.casha.app.core.network

import com.casha.app.core.config.AppConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that handles:
 * - 401 Unauthorized detection (broadcasts event for auto-logout)
 * - Error response parsing into structured [NetworkError]
 * - Debug request/response logging with emojis (like iOS APIClient)
 *
 * Equivalent to iOS `APIClient.handleError()` + debug printing.
 */
@Singleton
class ErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // ── Debug: Log Request ──
        if (AppConfig.shouldLogNetworkRequests) {
            AppConfig.log("""
                |
                |🔵 === REQUEST ===
                |🔵 URL: ${request.url}
                |🔵 METHOD: ${request.method}
                |🔵 HEADERS: ${request.headers}
                |🔵 ================
            """.trimMargin(), AppConfig.LogLevel.DEBUG)
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: java.net.SocketTimeoutException) {
            throw NetworkError.Timeout()
        } catch (e: java.net.UnknownHostException) {
            throw NetworkError.NoConnection()
        } catch (e: java.net.ConnectException) {
            throw NetworkError.NoConnection()
        } catch (e: Exception) {
            throw NetworkError.Unknown(e)
        }

        val statusCode = response.code
        val statusEmoji = if (statusCode in 200..299) "✅" else "❌"

        // ── Debug: Log Response ──
        if (AppConfig.shouldLogNetworkRequests) {
            // We need to peek at the body without consuming it
            val body = response.peekBody(Long.MAX_VALUE).string()
            AppConfig.log("""
                |
                |$statusEmoji === RESPONSE ===
                |$statusEmoji URL: ${request.url}
                |$statusEmoji STATUS: $statusCode
                |$statusEmoji BODY: $body
                |$statusEmoji ================
            """.trimMargin(),
                if (statusCode in 200..299) AppConfig.LogLevel.DEBUG
                else AppConfig.LogLevel.ERROR
            )
        }

        // ── Handle 401 Unauthorized ──
        if (statusCode == 401) {
            // Post event for auto-logout (similar to iOS NotificationCenter)
            UnauthorizedEvent.emit()
            throw NetworkError.Unauthorized()
        }

        // ── Handle other errors (4xx, 5xx) ──
        if (statusCode !in 200..299) {
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            val errorMessage = parseErrorMessage(errorBody) ?: "Request failed ($statusCode)"

            AppConfig.log("🔴 ERROR: $errorMessage (HTTP $statusCode)", AppConfig.LogLevel.ERROR)

            // Return the response so Retrofit can still parse it
            // (BaseResponse has code/status/message for graceful handling)
        }

        return response
    }

    /**
     * Tries to extract error message from JSON response body.
     * Supports: `{ "message": "..." }` or `{ "error": "..." }`
     */
    private fun parseErrorMessage(body: String): String? {
        return try {
            val json = Json.parseToJsonElement(body).jsonObject
            json["message"]?.jsonPrimitive?.content
                ?: json["error"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}
