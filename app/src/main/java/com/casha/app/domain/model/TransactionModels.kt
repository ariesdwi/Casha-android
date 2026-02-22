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
    val liabilityId: String? = null
)

/**
 * Request model for creating or updating a transaction.
 */
data class TransactionRequest(
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val note: String? = null
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
