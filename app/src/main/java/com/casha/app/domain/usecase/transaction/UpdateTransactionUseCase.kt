package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionCasha) {
        val updatedTransaction = transaction.copy(
            isSynced = false,
            updatedAt = Date()
        )
        repository.saveTransaction(updatedTransaction)
    }
}
