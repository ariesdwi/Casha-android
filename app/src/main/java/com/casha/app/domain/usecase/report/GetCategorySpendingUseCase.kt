package com.casha.app.domain.usecase.report

import com.casha.app.domain.model.ChartCategorySpending
import com.casha.app.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

class GetCategorySpendingUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(startDate: Date, endDate: Date): List<ChartCategorySpending> {
        return repository.getCategorySpendings(startDate, endDate)
    }
}
