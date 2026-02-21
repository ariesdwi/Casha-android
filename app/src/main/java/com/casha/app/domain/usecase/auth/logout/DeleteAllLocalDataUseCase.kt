package com.casha.app.domain.usecase.auth.logout

import com.casha.app.core.auth.AuthManager
import com.casha.app.data.local.database.CashaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case to clear all local data when a user logs out.
 */
class DeleteAllLocalDataUseCase @Inject constructor(
    private val authManager: AuthManager,
    private val database: CashaDatabase
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        authManager.clearAll()
        database.clearAllTables()
    }
}
