package com.casha.app.domain.usecase.auth

import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String
    ): String {
        return authRepository.register(name, email, phone, password)
    }
}
