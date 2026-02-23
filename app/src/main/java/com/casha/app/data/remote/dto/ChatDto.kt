package com.casha.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatRequestDto(
    val input: String
)

@Serializable
data class ChatResponseDto(
    val code: Int,
    val status: String,
    val message: String,
    val data: ChatParseDataDto? = null
)

@Serializable
data class ChatParseDataDto(
    val intent: String,
    val data: JsonElement
)

@Serializable
data class ChatTransactionDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ChatIncomeDto(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val type: String? = null,
    val source: String? = null,
    val frequency: String? = null,
    val isRecurring: Boolean = false,
    val note: String? = null,
    val assetId: String? = null,
    val assetName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
