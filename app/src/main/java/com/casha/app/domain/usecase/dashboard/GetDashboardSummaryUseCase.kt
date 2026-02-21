package com.casha.app.domain.usecase.dashboard

import javax.inject.Inject

/**
 * Use case to fetch a summary for the dashboard (e.g., total balance, monthly spending).
 */
class GetDashboardSummaryUseCase @Inject constructor() {
    suspend operator fun invoke() {
        // TODO: Call TransactionRepository and IncomeRepository to get summary
    }
}
