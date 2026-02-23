package com.casha.app.domain.usecase.liability

import com.casha.app.domain.model.LiabilityStatement
import com.casha.app.domain.repository.LiabilityRepository
import javax.inject.Inject

class GetStatementDetailsUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String, statementId: String): LiabilityStatement {
        return repository.getStatementDetails(liabilityId, statementId)
    }
}
