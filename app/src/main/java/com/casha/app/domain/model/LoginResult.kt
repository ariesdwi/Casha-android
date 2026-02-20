package com.casha.app.domain.model

/**
 * Result from a successful login or SSO authentication.
 */
data class LoginResult(
    val token: String,
    val currency: String?
)
