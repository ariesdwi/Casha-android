package com.casha.app.domain.repository

import com.casha.app.domain.model.EmailSyncStatus

interface EmailSyncRepository {
    suspend fun connectOAuth(serverAuthCode: String): Result<Unit>
    suspend fun getStatus(): Result<EmailSyncStatus>
    suspend fun disconnect(): Result<Unit>
}
