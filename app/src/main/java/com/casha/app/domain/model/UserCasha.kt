package com.casha.app.domain.model

import java.util.Date

/**
 * Domain model representing a Casha user.
 */
data class UserCasha(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val phone: String?,
    val currency: String,
    val createdAt: Date,
    val updatedAt: Date
)
