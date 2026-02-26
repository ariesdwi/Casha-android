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
import com.casha.app.core.network.safeApiCall
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
        val result = safeApiCall { apiService.login(LoginRequestDTO(email, password)) }
        return result.fold(
            onSuccess = { response -> 
                response.data?.toLoginResult() ?: throw Exception(response.message)
            },
            onFailure = { error -> throw error }
        )
    }

    override suspend fun googleLogin(idToken: String): LoginResult {
        val result = safeApiCall { apiService.googleLogin(GoogleLoginRequestDTO(idToken)) }
        return result.fold(
            onSuccess = { response -> 
                response.data?.toLoginResult() ?: throw Exception(response.message)
            },
            onFailure = { error -> throw error }
        )
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): String {
        val result = safeApiCall {
            apiService.register(
                RegisterRequestDTO(name, email, phone, password)
            )
        }
        return result.fold(
            onSuccess = { response ->
                response.data?.accessToken ?: throw Exception(response.message)
            },
            onFailure = { error -> throw error }
        )
    }

    override suspend fun resetPassword(email: String) {
        val result = safeApiCall { apiService.resetPassword(ResetPasswordRequestDTO(email)) }
        result.onSuccess { response ->
            if (response.code != 200 && response.code != 201) {
                throw Exception(response.message)
            }
        }.onFailure { throw it }
    }

    override suspend fun getProfile(): UserCasha {
        val result = safeApiCall { apiService.getProfile() }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw Exception(response.message)
            },
            onFailure = { error -> throw error }
        )
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): UserCasha {
        val result = safeApiCall { apiService.updateProfile(request.toDTO()) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw Exception(response.message)
            },
            onFailure = { error -> throw error }
        )
    }

    override suspend fun deleteAccount() {
        val result = safeApiCall { apiService.deleteAccount() }
        result.onSuccess { response ->
            if (response.code != 200 && response.code != 201) {
                throw Exception(response.message)
            }
        }.onFailure { throw it }
    }

    override suspend fun registerPushToken(token: String) {
        val result = safeApiCall { 
            apiService.registerPushToken(com.casha.app.data.remote.dto.RegisterTokenRequestDTO(token)) 
        }
        result.onSuccess { response ->
            if (response.code != 200 && response.code != 201) {
                throw Exception(response.message)
            }
        }.onFailure { throw it }
    }
}
