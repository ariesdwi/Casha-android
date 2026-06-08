package com.casha.app.domain.usecase.report

import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTransactionsByDateUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(date: LocalDate): List<TransactionCasha> {
        return repository.getTransactionsByDate(date)
    }
}
