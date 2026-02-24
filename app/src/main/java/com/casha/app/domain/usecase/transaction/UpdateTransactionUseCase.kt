package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

import com.casha.app.data.remote.dto.UpdateTransactionDto
import java.text.SimpleDateFormat
import java.util.Locale

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionCasha) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val dto = UpdateTransactionDto(
            name = transaction.name,
            category = transaction.category,
            amount = transaction.amount,
            datetime = dateFormat.format(transaction.datetime)
        )
        repository.updateTransaction(transaction.id, dto)
    }
}
