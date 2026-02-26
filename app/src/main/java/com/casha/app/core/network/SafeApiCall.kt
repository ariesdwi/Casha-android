package com.casha.app.core.network

import com.casha.app.core.config.AppConfig
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Executes a network call safely, catching exceptions and wrapping them in [Result<T>].
 * Converts standard IOExceptions into [NetworkError].
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: NetworkError) {
        // Already a structured error (from Interceptor)
        Result.failure(e)
    } catch (e: UnknownHostException) {
        Result.failure(NetworkError.NoConnection())
    } catch (e: SocketTimeoutException) {
        Result.failure(NetworkError.Timeout())
    } catch (e: IOException) {
        Result.failure(NetworkError.RequestFailed(e.message ?: "Network failure"))
    } catch (e: Exception) {
        AppConfig.log("Unknown network error: ${e.message}", AppConfig.LogLevel.ERROR)
        Result.failure(NetworkError.Unknown(e))
    }
}
