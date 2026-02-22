package com.casha.app.domain.usecase.report

import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

class GetTransactionByCategoryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(category: String, startDate: Date, endDate: Date): List<TransactionCasha> {
        return repository.getTransactionsByCategory(category, startDate, endDate)
    }
}
