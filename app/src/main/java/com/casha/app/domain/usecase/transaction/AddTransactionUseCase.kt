package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.TransactionRequest
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(request: TransactionRequest) {
        val transaction = TransactionCasha(
            id = UUID.randomUUID().toString(),
            name = request.name,
            category = request.category,
            amount = request.amount,
            datetime = request.datetime,
            note = request.note,
            isSynced = false,
            createdAt = Date(),
            updatedAt = Date()
        )
        repository.saveTransaction(transaction)
    }
}
