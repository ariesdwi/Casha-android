package com.casha.app.domain.model

/**
 * Result from a register (sign-up) call.
 */
data class RegisterResult(
    val message: String,
    /** True if account already existed but was unverified (verification email re-sent). */
    val userExists: Boolean = false,
    /** True if the existing account was already verified (409 Conflict path). */
    val isVerified: Boolean = false
)
