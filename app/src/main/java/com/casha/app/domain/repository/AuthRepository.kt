package com.casha.app.domain.repository

import com.casha.app.domain.model.LoginResult
import com.casha.app.domain.model.UpdateProfileRequest
import com.casha.app.domain.model.UserCasha

/**
 * Repository interface for authentication and user profile operations.
 */
interface AuthRepository {

    /** Email + password login. */
    suspend fun login(email: String, password: String): LoginResult

    /** Google SSO login with ID token. */
    suspend fun googleLogin(idToken: String): LoginResult

    /** Register a new account. Returns access token. */
    suspend fun register(name: String, email: String, phone: String, password: String): String

    /** Send password reset email. */
    suspend fun resetPassword(email: String)

    /** Fetch the current user's profile. */
    suspend fun getProfile(): UserCasha

    /** Update the current user's profile. */
    suspend fun updateProfile(request: UpdateProfileRequest): UserCasha

    /** Delete the current user's account. */
    suspend fun deleteAccount()

    /** Register FCM push token for the current user. */
    suspend fun registerPushToken(token: String)
}
