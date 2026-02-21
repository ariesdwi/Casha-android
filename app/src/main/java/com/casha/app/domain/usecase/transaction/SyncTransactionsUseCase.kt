package com.casha.app.domain.usecase.transaction

import com.casha.app.domain.repository.TransactionRepository
import javax.inject.Inject

class SyncTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke() {
        repository.syncTransactions()
    }
}
