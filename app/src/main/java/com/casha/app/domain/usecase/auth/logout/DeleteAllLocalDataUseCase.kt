package com.casha.app.domain.usecase.auth.logout

import android.content.Context
import com.casha.app.core.auth.AuthManager
import com.casha.app.data.local.database.CashaDatabase
import com.casha.app.domain.repository.NotificationRepository
import com.casha.app.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case to clear all local data when a user logs out.
 */
class DeleteAllLocalDataUseCase @Inject constructor(
    private val authManager: AuthManager,
    private val database: CashaDatabase,
    private val notificationRepository: NotificationRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val pendingToken = authManager.pendingFcmToken.firstOrNull()
        if (!pendingToken.isNullOrBlank()) {
            try {
                notificationRepository.unregisterToken(pendingToken)
                android.util.Log.d("DeleteData", "Successfully unregistered FCM token.")
            } catch (e: Exception) {
                android.util.Log.e("DeleteData", "Failed to unregister FCM token: ${e.message}")
            }
        }
        
        authManager.clearAll()
        database.clearAllTables()

        // Reset widget state on logout
        WidgetUpdater.setAuthState(context, isLoggedIn = false, isPremium = false)
        WidgetUpdater.stopPeriodicRefresh(context)
    }
}
