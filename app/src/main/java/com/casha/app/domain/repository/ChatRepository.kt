package com.casha.app.domain.repository

import com.casha.app.domain.model.ChatParseResult
import java.io.File

interface ChatRepository {
    suspend fun parseChat(input: String): ChatParseResult
    suspend fun parseImage(file: File): ChatParseResult
}
