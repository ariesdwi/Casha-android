package com.casha.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for a transaction from/to the server.
 */
@Serializable
data class TransactionDto(
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

/**
 * DTO for creating or updating a transaction.
 */
@Serializable
data class TransactionUploadDto(
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val note: String? = null
)

/**
 * DTO for partially updating an existing transaction.
 */
@Serializable
data class UpdateTransactionDto(
    val name: String = "",
    val amount: Double = 0.0,
    val category: String? = null,
    val datetime: String = ""
)

/**
 * Standard response for transaction listing.
 */
@Serializable
data class TransactionListResponse(
    val transactions: List<TransactionDto> = emptyList()
)
