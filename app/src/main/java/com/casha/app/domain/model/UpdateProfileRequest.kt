package com.casha.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Request body for updating user profile.
 * All fields are optional — only non-null fields are sent to the API.
 */
@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val currency: String? = null
)
