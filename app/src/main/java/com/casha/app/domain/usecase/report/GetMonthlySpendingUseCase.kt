package com.casha.app.domain.usecase.report

import com.casha.app.domain.model.MonthlySpending
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

class GetMonthlySpendingUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(startDate: Date, endDate: Date): List<MonthlySpending> {
        return repository.getMonthlySpending(startDate, endDate)
    }
}
