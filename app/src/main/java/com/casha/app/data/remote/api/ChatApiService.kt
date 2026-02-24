package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.ChatRequestDto
import com.casha.app.data.remote.dto.ChatResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ChatApiService {
    @POST("chat/parse")
    suspend fun parseChat(
        @Body request: ChatRequestDto
    ): ChatResponseDto

    @Multipart
    @POST("chat/parse-image")
    suspend fun parseImage(
        @Part file: MultipartBody.Part,
        @Part("input") input: okhttp3.RequestBody
    ): ChatResponseDto
}
