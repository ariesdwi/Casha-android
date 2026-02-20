package com.casha.app.domain.usecase.auth

import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.resetPassword(email)
    }
}
