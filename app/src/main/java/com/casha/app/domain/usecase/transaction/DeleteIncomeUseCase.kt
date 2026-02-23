package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.repository.IncomeRepository
import javax.inject.Inject

class DeleteIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteIncome(id)
    }
}
