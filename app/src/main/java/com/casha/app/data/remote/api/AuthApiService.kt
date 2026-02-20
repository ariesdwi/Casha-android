package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.BaseResponse
import com.casha.app.data.remote.dto.GoogleLoginRequestDTO
import com.casha.app.data.remote.dto.LoginRequestDTO
import com.casha.app.data.remote.dto.LoginResponseDTO
import com.casha.app.data.remote.dto.ProfileDTO
import com.casha.app.data.remote.dto.RegisterRequestDTO
import com.casha.app.data.remote.dto.RegisterResponseDTO
import com.casha.app.data.remote.dto.ResetPasswordRequestDTO
import com.casha.app.data.remote.dto.UpdateProfileRequestDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Retrofit service interface for Auth API endpoints.
 */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDTO): BaseResponse<LoginResponseDTO>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequestDTO): BaseResponse<LoginResponseDTO>

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequestDTO): BaseResponse<RegisterResponseDTO>

    @POST("auth/forgot-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDTO): BaseResponse<Unit>

    @GET("auth/profile")
    suspend fun getProfile(): BaseResponse<ProfileDTO>

    @PATCH("auth/profile/")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDTO): BaseResponse<ProfileDTO>

    @DELETE("auth/profile/")
    suspend fun deleteAccount(): BaseResponse<Unit>
}
