package com.casha.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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
    val data: JsonElement,
    val message: String? = null,
    val summary: MultiExpenseSummaryDto? = null
)

@Serializable
data class ChatTransactionDto(
    val id: String = "",
    val name: String = "",
    val category: JsonElement? = null,
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val groupId: String? = null,
    val groupName: String? = null
) {
    /** Extract category name whether the field is a plain string or {"id":…,"name":…} object. */
    val categoryName: String
        get() = when {
            category == null -> ""
            category is JsonPrimitive -> (category as JsonPrimitive).contentOrNull ?: ""
            category is JsonObject -> {
                (category as JsonObject)["name"]
                    ?.let { (it as? JsonPrimitive)?.contentOrNull }
                    ?: ""
            }
            else -> ""
        }
}

@Serializable
data class MultiExpenseSummaryDto(
    val groupId: String = "",
    val groupName: String = "",
    val count: Int = 0,
    val total: Double = 0.0,
    val currency: String = ""
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
