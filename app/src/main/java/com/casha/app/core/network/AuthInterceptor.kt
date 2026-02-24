package com.casha.app.core.network

import com.casha.app.core.auth.AuthManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that injects:
 * - Authorization: Bearer {token}
 * - Accept-Language: {device locale}
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { authManager.accessToken.firstOrNull() }
        val locale = Locale.getDefault().toLanguageTag()

        val newRequest = originalRequest.newBuilder().apply {
            header("Accept", "application/json")
            header("Accept-Language", locale)
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
