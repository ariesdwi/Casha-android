package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.IncomeCasha
import com.casha.app.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIncomesUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    operator fun invoke(): Flow<List<IncomeCasha>> {
        return repository.getIncomesFlow()
    }
}
