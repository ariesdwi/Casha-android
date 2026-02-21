package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<TransactionCasha>> {
        return repository.getTransactions()
    }
}
