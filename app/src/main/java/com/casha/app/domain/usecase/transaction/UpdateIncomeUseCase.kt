package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.CreateIncomeRequest
import com.casha.app.domain.repository.IncomeRepository
import javax.inject.Inject

class UpdateIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    suspend operator fun invoke(id: String, request: CreateIncomeRequest) {
        repository.updateIncome(id, request)
    }
}
