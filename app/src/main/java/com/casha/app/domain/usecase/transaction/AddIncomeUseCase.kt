package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.CreateIncomeRequest
import com.casha.app.domain.repository.IncomeRepository
import javax.inject.Inject

class AddIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    suspend operator fun invoke(request: CreateIncomeRequest) {
        repository.saveIncome(request)
    }
}
