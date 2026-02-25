package com.casha.app.data.remote.impl

import com.casha.app.core.network.NetworkError
import com.casha.app.data.remote.api.LiabilityApiService
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.LiabilityRepository
import java.text.SimpleDateFormat
import java.util.Locale
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

        val response = api.createLiability(body)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun getLiabilities(status: String?, sortBy: String?, sortOrder: String?): List<Liability> {
        val response = api.getLiabilities(status = status, sortBy = sortBy, sortOrder = sortOrder)
        return response.data?.liabilities?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getLiabilitySummary(): LiabilitySummary {
        val response = api.getLiabilitySummary()
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun getLiabilityDetails(id: String): Liability {
        val response = api.getLiabilityDetails(id)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun deleteLiability(id: String) {
        api.deleteLiability(id)
    }

    override suspend fun recordPayment(liabilityId: String, request: CreateLiabilityPaymentRequest): LiabilityPayment {
        val body = mutableMapOf<String, Any>(
            "liabilityId" to liabilityId,
            "amount" to request.amount,
            "paymentDate" to simpleDateFormatter.format(request.paymentDate)
        )
        
        request.paymentType?.let { body["paymentType"] = it.rawValue }
        request.notes?.let { body["notes"] = it }
        
        val response = api.recordPayment(liabilityId, body)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun createTransaction(liabilityId: String, request: CreateLiabilityTransactionRequest): LiabilityTransaction {
        val body = mutableMapOf<String, Any>(
            "name" to request.name,
            "amount" to request.amount,
            "categoryId" to request.categoryId,
            "datetime" to dateFormatter.format(request.datetime)
        )
        
        request.description?.let { body["description"] = it }
        
        val response = api.createTransaction(liabilityId, body)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun getTransactions(liabilityId: String, page: Int, pageSize: Int): List<LiabilityTransaction> {
        val response = api.getTransactions(liabilityId, page, pageSize)
        return response.data?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getPaymentHistory(liabilityId: String, page: Int, pageSize: Int): LiabilityPaymentHistory {
        val response = api.getPaymentHistory(liabilityId, page, pageSize)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun getUnbilledTransactions(liabilityId: String): UnbilledTransactions {
        val response = api.getUnbilledTransactions(liabilityId)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun getLatestStatement(liabilityId: String): LiabilityStatement? {
        val response = api.getLatestStatement(liabilityId)
        return response.data?.toDomain()
    }

    override suspend fun getAllStatements(liabilityId: String): List<LiabilityStatement> {
        val response = api.getAllStatements(liabilityId)
        return response.data?.statements?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getStatementDetails(liabilityId: String, statementId: String): LiabilityStatement {
        val response = api.getStatementDetails(liabilityId, statementId)
        val data = response.data ?: throw NetworkError.RequestFailed("Invalid response")
        return com.casha.app.data.remote.dto.LiabilityStatementDTO.fromDetailData(data, liabilityId)
    }

    override suspend fun getLiabilityInsights(liabilityId: String): LiabilityInsight {
        val response = api.getLiabilityInsights(liabilityId)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun convertToInstallment(liabilityId: String, transactionId: String, tenor: Int): InstallmentPlan {
        val body = mapOf("tenor" to tenor)
        val response = api.convertToInstallment(liabilityId, transactionId, body)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
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
        val response = api.addInstallment(liabilityId, body)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }

    override suspend fun simulatePayoff(request: SimulatePayoffRequest): SimulatePayoffResponse {
        val body = mutableMapOf<String, Any>(
            "strategy" to request.strategy.rawValue
        )
        request.additionalPayment?.let { body["additionalPayment"] = it }
        
        val response = api.simulatePayoff(body)
        return response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
    }
}
