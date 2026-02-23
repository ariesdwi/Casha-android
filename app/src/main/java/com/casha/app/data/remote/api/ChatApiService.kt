package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.ChatRequestDto
import com.casha.app.data.remote.dto.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("chat/parse")
    suspend fun parseChat(
        @Body request: ChatRequestDto
    ): ChatResponseDto
}
