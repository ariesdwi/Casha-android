package com.casha.app.domain.usecase.liability

import com.casha.app.domain.model.Liability
import com.casha.app.domain.repository.LiabilityRepository
import javax.inject.Inject

interface GetLiabilityDetailsUseCase {
    suspend fun execute(id: String): Liability
}

class GetLiabilityDetailsUseCaseImpl @Inject constructor(
    private val repository: LiabilityRepository
) : GetLiabilityDetailsUseCase {
    override suspend fun execute(id: String): Liability {
        return repository.getLiabilityDetails(id)
    }
}
