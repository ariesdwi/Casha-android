package com.casha.app.domain.usecase.auth

import com.casha.app.domain.model.LoginResult
import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): LoginResult {
        return authRepository.login(email, password)
    }
}
