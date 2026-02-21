package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteTransaction(id)
    }
}
