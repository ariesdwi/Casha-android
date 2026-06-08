package com.casha.app.domain.usecase.report

import com.casha.app.domain.model.DailySpending
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

class GetDailySpendingUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(startDate: Date, endDate: Date): List<DailySpending> {
        return repository.getDailySpending(startDate, endDate)
    }
}
