package com.casha.app.domain.model

import java.util.Date

/**
 * Domain model for a category, mirroring the iOS CategoryCasha definition.
 */
data class CategoryCasha(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val userId: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
