package com.casha.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import com.casha.app.domain.model.*

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isActive: Boolean = true,
    val userId: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val amount: Double,
    val datetime: Date,
    val type: IncomeType,
    val source: String?,
    val assetId: String?,
    val isRecurring: Boolean = false,
    val frequency: IncomeFrequency?,
    val note: String?,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
