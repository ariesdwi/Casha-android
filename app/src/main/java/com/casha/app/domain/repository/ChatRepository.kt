package com.casha.app.domain.repository

import com.casha.app.domain.model.ChatParseResult

interface ChatRepository {
    suspend fun parseChat(input: String): ChatParseResult
}
