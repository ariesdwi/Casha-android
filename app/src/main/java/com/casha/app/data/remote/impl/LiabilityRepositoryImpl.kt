package com.casha.app.data.remote.impl

import com.casha.app.core.network.NetworkError
import com.casha.app.data.remote.api.LiabilityApiService
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.LiabilityRepository
import java.text.SimpleDateFormat
import java.util.Locale
import com.casha.app.core.network.safeApiCall
import javax.inject.Inject

class LiabilityRepositoryImpl @Inject constructor(
    private val api: LiabilityApiService
) : LiabilityRepository {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    private val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun createLiability(request: CreateLiabilityRequest): Liability {
        val body = mutableMapOf<String, Any>(
            "name" to request.name,
            "category" to request.category.rawValue,
            "interestRate" to request.interestRate,
            "principal" to request.principal
        )
        
        request.startDate?.let { body["startDate"] = it }
        request.endDate?.let { body["endDate"] = it }
        request.bankName?.let { body["bankName"] = it }
        request.creditLimit?.let { body["creditLimit"] = it }
        request.billingDay?.let { body["billingDay"] = it }
        request.dueDay?.let { body["dueDay"] = it }
        request.minPaymentPercentage?.let { body["minPaymentPercentage"] = it }
        request.interestType?.let { body["interestType"] = it.rawValue }
        request.lateFee?.let { body["lateFee"] = it }
        request.currentBalance?.let { body["currentBalance"] = it }
        request.description?.let { body["description"] = it }
        request.tenor?.let { body["tenor"] = it }
        request.monthlyInstallment?.let { body["monthlyInstallment"] = it }
        request.gracePeriodMonths?.let { body["gracePeriodMonths"] = it }

        val result = safeApiCall { api.createLiability(body) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getLiabilities(status: String?, sortBy: String?, sortOrder: String?): List<Liability> {
        val result = safeApiCall { api.getLiabilities(status = status, sortBy = sortBy, sortOrder = sortOrder) }
        return result.fold(
            onSuccess = { response ->
                response.data?.liabilities?.map { it.toDomain() } ?: emptyList()
            },
            onFailure = { emptyList() } // Return empty list on failure for safety
        )
    }

    override suspend fun getLiabilitySummary(): LiabilitySummary {
        val result = safeApiCall { api.getLiabilitySummary() }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getLiabilityDetails(id: String): Liability {
        val result = safeApiCall { api.getLiabilityDetails(id) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun deleteLiability(id: String) {
        safeApiCall { api.deleteLiability(id) }
    }

    override suspend fun recordPayment(liabilityId: String, request: CreateLiabilityPaymentRequest): LiabilityPayment {
        val body = mutableMapOf<String, Any>(
            "liabilityId" to liabilityId,
            "amount" to request.amount,
            "paymentDate" to simpleDateFormatter.format(request.paymentDate)
        )
        
        request.paymentType?.let { body["paymentType"] = it.rawValue }
        request.notes?.let { body["notes"] = it }
        
        val result = safeApiCall { api.recordPayment(liabilityId, body) }
        return result.fold(
            onSuccess = { response -> 
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun createTransaction(liabilityId: String, request: CreateLiabilityTransactionRequest): LiabilityTransaction {
        val body = mutableMapOf<String, Any>(
            "name" to request.name,
            "amount" to request.amount,
            "categoryId" to request.categoryId,
            "datetime" to dateFormatter.format(request.datetime)
        )
        
        request.description?.let { body["description"] = it }
        
        val result = safeApiCall { api.createTransaction(liabilityId, body) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getTransactions(liabilityId: String, page: Int, pageSize: Int): List<LiabilityTransaction> {
        val result = safeApiCall { api.getTransactions(liabilityId, page, pageSize) }
        return result.fold(
            onSuccess = { response -> 
                response.data?.map { it.toDomain() } ?: emptyList() 
            },
            onFailure = { emptyList() }
        )
    }

    override suspend fun getPaymentHistory(liabilityId: String, page: Int, pageSize: Int): LiabilityPaymentHistory {
        val result = safeApiCall { api.getPaymentHistory(liabilityId, page, pageSize) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getUnbilledTransactions(liabilityId: String): UnbilledTransactions {
        val result = safeApiCall { api.getUnbilledTransactions(liabilityId) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getLatestStatement(liabilityId: String): LiabilityStatement? {
        val result = safeApiCall { api.getLatestStatement(liabilityId) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() },
            onFailure = { null }
        )
    }

    override suspend fun getAllStatements(liabilityId: String): List<LiabilityStatement> {
        val result = safeApiCall { api.getAllStatements(liabilityId) }
        return result.fold(
            onSuccess = { response -> response.data?.statements?.map { it.toDomain() } ?: emptyList() },
            onFailure = { emptyList() }
        )
    }

    override suspend fun getStatementDetails(liabilityId: String, statementId: String): LiabilityStatement {
        val result = safeApiCall { api.getStatementDetails(liabilityId, statementId) }
        return result.fold(
            onSuccess = { response -> 
                val data = response.data ?: throw NetworkError.RequestFailed("Invalid response")
                com.casha.app.data.remote.dto.LiabilityStatementDTO.fromDetailData(data, liabilityId)
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getLiabilityInsights(liabilityId: String): LiabilityInsight {
        val result = safeApiCall { api.getLiabilityInsights(liabilityId) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun convertToInstallment(liabilityId: String, transactionId: String, tenor: Int): InstallmentPlan {
        val body = mapOf("tenor" to tenor)
        val result = safeApiCall { api.convertToInstallment(liabilityId, transactionId, body) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun addInstallment(liabilityId: String, request: CreateLiabilityInstallmentRequest): InstallmentPlan {
        val body = mapOf(
            "name" to request.name,
            "totalAmount" to request.totalAmount,
            "monthlyAmount" to request.monthlyAmount,
            "tenor" to request.tenor,
            "currentMonth" to request.currentMonth,
            "startDate" to request.startDate
        )
        val result = safeApiCall { api.addInstallment(liabilityId, body) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun simulatePayoff(request: SimulatePayoffRequest): SimulatePayoffResponse {
        val body = mutableMapOf<String, Any>(
            "strategy" to request.strategy.rawValue
        )
        request.additionalPayment?.let { body["additionalPayment"] = it }
        
        val result = safeApiCall { api.simulatePayoff(body) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }
}
