package com.casha.app.domain.usecase.profile

import com.casha.app.domain.repository.AuthRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.deleteAccount()
    }
}
