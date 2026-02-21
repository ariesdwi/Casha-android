package com.casha.app.data.local.entity

import androidx.room.*
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val note: String? = null,
    val isSynced: Boolean = false,
    val remoteId: String? = null,

    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
