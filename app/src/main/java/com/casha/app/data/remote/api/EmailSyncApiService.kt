package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.BaseResponse
import com.casha.app.data.remote.dto.ConnectOAuthRequestDTO
import com.casha.app.data.remote.dto.EmailSyncStatusDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface EmailSyncApiService {

    @POST("email-sync/connect-oauth")
    suspend fun connectOAuth(@Body request: ConnectOAuthRequestDTO): BaseResponse<Unit>

    @GET("email-sync/status")
    suspend fun getStatus(): BaseResponse<EmailSyncStatusDTO>

    @DELETE("email-sync/disconnect")
    suspend fun disconnect(): BaseResponse<Unit>
}
