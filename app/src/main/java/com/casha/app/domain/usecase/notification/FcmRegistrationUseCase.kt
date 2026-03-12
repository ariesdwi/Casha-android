package com.casha.app.domain.usecase.notification

import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.repository.AuthRepository
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Ensures FCM tokens are only sent to the backend if the user is actively logged in.
 *
 * Flow:
 * 1. Checks if a valid JWT `accessToken` exists.
 * 2. Checks if there is a `pendingFcmToken` waiting to be registered.
 * 3. If both exist, sends the token to the backend.
 * 4. On success, clears the pending token from LocalStorage/DataStore.
 * 5. On 401 Unauthorized, forcibly logs the user out.
 */
class FcmRegistrationUseCase @Inject constructor(
    private val authManager: AuthManager,
    private val authRepository: AuthRepository,
    private val deleteAllLocalDataUseCase: DeleteAllLocalDataUseCase
) {
    suspend operator fun invoke() {
        val jwt = authManager.accessToken.firstOrNull()
        val pendingToken = authManager.pendingFcmToken.firstOrNull()

        android.util.Log.d("FCM", "FcmRegistrationUseCase: jwt=${if (jwt.isNullOrBlank()) "MISSING" else "PRESENT"}, pendingToken=${if (pendingToken.isNullOrBlank()) "MISSING" else pendingToken}")

        // Requirement: Never call register-token unless you have a valid JWT.
        if (jwt.isNullOrBlank() || pendingToken.isNullOrBlank()) {
            android.util.Log.w("FCM", "Skipping register-token: jwt=${if (jwt.isNullOrBlank()) "MISSING" else "PRESENT"}, pendingToken=${if (pendingToken.isNullOrBlank()) "MISSING" else "PRESENT"}")
            return
        }

        try {
            // Register token with backend (uses AuthInterceptor which attaches the JWT)
            authRepository.registerPushToken(pendingToken)
            
            // On success → clear pendingFCMToken
            authManager.clearPendingFcmToken()
            
            android.util.Log.d("FCM", "Safely registered pending FCM token: $pendingToken")
        } catch (e: Exception) {
            android.util.Log.e("FCM", "Failed to register FCM token: ${e.message}")
            
            // On 401 → force logout, go to LOGIN FLOW
            if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                authManager.clearPendingFcmToken()
                deleteAllLocalDataUseCase()
            }
        }
    }
}
