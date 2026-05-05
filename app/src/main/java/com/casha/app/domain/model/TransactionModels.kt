package com.casha.app.domain.model

import java.util.Date

/**
 * Core domain model for a transaction in Casha.
 */
data class TransactionCasha(
    val id: String,
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val note: String? = null,
    val isSynced: Boolean = false,
    val remoteId: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val liabilityId: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val assetId: String? = null
)

/**
 * Request model for creating or updating a transaction.
 */
data class TransactionRequest(
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val note: String? = null,
    val assetId: String? = null
)

/**
 * Request model for partially updating an existing transaction.
 */
data class UpdateTransactionRequest(
    val name: String,
    val amount: Double,
    val category: String?,
    val datetime: String
)

enum class ChatParseIntent(val rawValue: String) {
    EXPENSE("EXPENSE"), 
    INCOME("INCOME"), 
    PAYMENT("PAYMENT"),
    MULTI_EXPENSE("MULTI_EXPENSE"),
    UNKNOWN("UNKNOWN")
}

data class MultiExpenseSummary(
    val groupId: String,
    val groupName: String,
    val count: Int,
    val total: Double,
    val currency: String
)

data class ChatParseResult(
    val intent: ChatParseIntent,
    val message: String,
    val expenses: List<TransactionCasha>? = null,
    val summary: MultiExpenseSummary? = null
)
