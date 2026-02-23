package com.casha.app.data.remote.impl

import com.casha.app.data.remote.api.AuthApiService
import com.casha.app.data.remote.dto.GoogleLoginRequestDTO
import com.casha.app.data.remote.dto.LoginRequestDTO
import com.casha.app.data.remote.dto.RegisterRequestDTO
import com.casha.app.data.remote.dto.ResetPasswordRequestDTO
import com.casha.app.data.remote.dto.toDTO
import com.casha.app.data.remote.dto.toDomain
import com.casha.app.data.remote.dto.toLoginResult
import com.casha.app.domain.model.LoginResult
import com.casha.app.domain.model.UpdateProfileRequest
import com.casha.app.domain.model.UserCasha
import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AuthRepository] using Retrofit API service.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginResult {
        val response = apiService.login(LoginRequestDTO(email, password))
        return response.data?.toLoginResult()
            ?: throw Exception(response.message)
    }

    override suspend fun googleLogin(idToken: String): LoginResult {
        val response = apiService.googleLogin(GoogleLoginRequestDTO(idToken))
        return response.data?.toLoginResult()
            ?: throw Exception(response.message)
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): String {
        val response = apiService.register(
            RegisterRequestDTO(name, email, phone, password)
        )
        return response.data?.accessToken
            ?: throw Exception(response.message)
    }

    override suspend fun resetPassword(email: String) {
        val response = apiService.resetPassword(ResetPasswordRequestDTO(email))
        if (response.code != 200 && response.code != 201) {
            throw Exception(response.message)
        }
    }

    override suspend fun getProfile(): UserCasha {
        val response = apiService.getProfile()
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): UserCasha {
        val response = apiService.updateProfile(request.toDTO())
        return response.data?.toDomain()
            ?: throw Exception(response.message)
    }

    override suspend fun deleteAccount() {
        val response = apiService.deleteAccount()
        if (response.code != 200 && response.code != 201) {
            throw Exception(response.message)
        }
    }

    override suspend fun registerPushToken(token: String) {
        val response = apiService.registerPushToken(com.casha.app.data.remote.dto.RegisterTokenRequestDTO(token))
        if (response.code != 200 && response.code != 201) {
            throw Exception(response.message)
        }
    }
}
