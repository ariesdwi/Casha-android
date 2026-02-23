package com.casha.app.domain.repository

import com.casha.app.domain.model.*

/**
 * Interface defining the comprehensive data operations for the Liabilities feature.
 */
interface LiabilityRepository {
    
    // Core operations
    suspend fun createLiability(request: CreateLiabilityRequest): Liability
    suspend fun getLiabilities(): List<Liability>
    suspend fun getLiabilitySummary(): LiabilitySummary
    suspend fun getLiabilityDetails(id: String): Liability
    suspend fun deleteLiability(id: String)
    
    // Payments & Transactions
    suspend fun recordPayment(liabilityId: String, request: CreateLiabilityPaymentRequest): LiabilityPayment
    suspend fun createTransaction(liabilityId: String, request: CreateLiabilityTransactionRequest): LiabilityTransaction
    suspend fun getTransactions(liabilityId: String, page: Int = 1, pageSize: Int = 20): List<LiabilityTransaction>
    suspend fun getPaymentHistory(liabilityId: String, page: Int = 1, pageSize: Int = 20): LiabilityPaymentHistory
    suspend fun getUnbilledTransactions(liabilityId: String): UnbilledTransactions
    
    // Statements
    suspend fun getLatestStatement(liabilityId: String): LiabilityStatement?
    suspend fun getAllStatements(liabilityId: String): List<LiabilityStatement>
    suspend fun getStatementDetails(liabilityId: String, statementId: String): LiabilityStatement
    
    // Utilities
    suspend fun getLiabilityInsights(liabilityId: String): LiabilityInsight
    suspend fun convertToInstallment(liabilityId: String, transactionId: String, tenor: Int): InstallmentPlan
}
