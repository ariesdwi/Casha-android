package com.casha.app.data.remote.impl

import com.casha.app.core.network.safeApiCall
import com.casha.app.data.remote.api.EmailSyncApiService
import com.casha.app.data.remote.dto.ConnectOAuthRequestDTO
import com.casha.app.data.remote.dto.EmailSyncStatusDTO
import com.casha.app.domain.model.EmailSyncStatus
import com.casha.app.domain.repository.EmailSyncRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailSyncRepositoryImpl @Inject constructor(
    private val apiService: EmailSyncApiService
) : EmailSyncRepository {

    override suspend fun connectOAuth(serverAuthCode: String): Result<Unit> {
        return safeApiCall {
            apiService.connectOAuth(ConnectOAuthRequestDTO(serverAuthCode))
        }.map { Unit }
    }

    override suspend fun getStatus(): Result<EmailSyncStatus> {
        return safeApiCall {
            apiService.getStatus()
        }.map { response ->
            val dto = response.data ?: EmailSyncStatusDTO()
            EmailSyncStatus(
                connected = dto.connected,
                emailAddress = dto.emailAddress,
                authType = dto.authType,
                isActive = dto.isActive,
                lastSyncAt = dto.lastSyncAt,
                bankSenders = dto.bankSenders
            )
        }
    }

    override suspend fun disconnect(): Result<Unit> {
        return safeApiCall {
            apiService.disconnect()
        }.map { Unit }
    }
}
