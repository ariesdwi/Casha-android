package com.casha.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val spent: Double,
    val remaining: Double,
    val period: String,
    val startDate: Date,
    val endDate: Date,
    val category: String,
    val currency: String,
    val isSynced: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
