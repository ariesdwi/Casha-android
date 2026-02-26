package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.BaseResponse
import com.casha.app.data.remote.dto.PushNotificationResponseDto
import com.casha.app.data.remote.dto.RegisterTokenRequestDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApiService {
    @POST("push-notifications/register-token")
    suspend fun registerToken(@Body request: RegisterTokenRequestDTO): BaseResponse<PushNotificationResponseDto>

    @DELETE("push-notifications/unregister-token/{token}")
    suspend fun unregisterToken(@Path("token") token: String): BaseResponse<PushNotificationResponseDto>
}
