package com.casha.app.domain.usecase.liability

import com.casha.app.domain.model.*
import com.casha.app.domain.repository.LiabilityRepository
import javax.inject.Inject

class GetLiabilitiesUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(): List<Liability> = repository.getLiabilities()
}

class GetLiabilitySummaryUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(): LiabilitySummary = repository.getLiabilitySummary()
}

class CreateLiabilityUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(request: CreateLiabilityRequest): Liability = repository.createLiability(request)
}

class RecordLiabilityPaymentUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String, request: CreateLiabilityPaymentRequest): LiabilityPayment {
        return repository.recordPayment(liabilityId, request)
    }
}

class GetLatestStatementUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String): LiabilityStatement? = repository.getLatestStatement(liabilityId)
}

class GetAllStatementsUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String): List<LiabilityStatement> = repository.getAllStatements(liabilityId)
}

class GetUnbilledTransactionsUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String): UnbilledTransactions = repository.getUnbilledTransactions(liabilityId)
}

class GetLiabilityInsightsUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String): LiabilityInsight = repository.getLiabilityInsights(liabilityId)
}

class GetLiabilityPaymentHistoryUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String, page: Int = 1, pageSize: Int = 20): LiabilityPaymentHistory {
        return repository.getPaymentHistory(liabilityId, page, pageSize)
    }
}

class GetLiabilityTransactionsUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String, page: Int = 1, pageSize: Int = 20): List<LiabilityTransaction> {
        return repository.getTransactions(liabilityId, page, pageSize)
    }
}

class CreateLiabilityTransactionUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(liabilityId: String, request: CreateLiabilityTransactionRequest): LiabilityTransaction {
        return repository.createTransaction(liabilityId, request)
    }
}

class DeleteLiabilityUseCase @Inject constructor(
    private val repository: LiabilityRepository
) {
    suspend fun execute(id: String) {
        repository.deleteLiability(id)
    }
}
