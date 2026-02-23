package com.casha.app.domain.usecase.profile

import com.casha.app.domain.model.UserCasha
import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserCasha {
        return authRepository.getProfile()
    }
}
