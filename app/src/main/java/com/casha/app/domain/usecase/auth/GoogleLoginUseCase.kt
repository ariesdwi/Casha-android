package com.casha.app.domain.usecase.auth

import com.casha.app.domain.model.LoginResult
import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): LoginResult {
        return authRepository.googleLogin(idToken)
    }
}
