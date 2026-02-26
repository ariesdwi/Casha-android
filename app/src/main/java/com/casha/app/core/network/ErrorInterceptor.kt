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
        } catch (e: java.io.IOException) {
            // Rethrow standard IOExceptions so OkHttp and Retrofit handle them normally
            throw e
        } catch (e: Exception) {
            // Wrap other unexpected exceptions
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
            val errorBody = try {
                response.peekBody(Long.MAX_VALUE).string()
            } catch (e: Exception) {
                "{\"message\": \"Could not read error body: ${e.message}\"}"
            }
            
            val errorMessage = try {
                val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                val jsonObj = jsonParser.parseToJsonElement(errorBody).jsonObject
                jsonObj["message"]?.jsonPrimitive?.content ?: "API Error: $statusCode"
            } catch (e: Exception) {
                "API Error: $statusCode"
            }
            
            throw NetworkError.ApiError(statusCode, errorMessage)
        }

        return response
    }
}
