package com.casha.app.data.remote.dto

import com.casha.app.domain.model.*
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class LiabilityInlineInstallmentPlanDTO(
    val id: String? = null,
    val name: String? = null,
    val totalAmount: Double? = null,
    val monthlyAmount: Double? = null,
    val tenor: Int? = null,
    val currentMonth: Int? = null,
    val startDate: String? = null,
    val isActive: Boolean? = null,
    val progress: Int? = null
) {
    fun toDomain(): InstallmentPlan {
        val sysFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedStart = startDate?.let {
            try { sysFormat.parse(it) } catch (e: Exception) {
                try { simpleFormat.parse(it) } catch (e2: Exception) { null }
            }
        } ?: Date()
        return InstallmentPlan(
            id = id ?: "",
            liabilityId = "",
            userId = "",
            name = name ?: "",
            totalAmount = totalAmount ?: 0.0,
            monthlyAmount = monthlyAmount ?: 0.0,
            tenor = tenor ?: 0,
            currentMonth = currentMonth ?: 0,
            isActive = isActive ?: false,
            startDate = parsedStart,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}

@Serializable
data class LiabilityInlineStatementDTO(
    val id: String? = null,
    val period: StatementPeriodDTO? = null,
    val summary: StatementSummaryDTO? = null,
    val status: String? = null
)

@Serializable
data class LiabilityDto(
    val id: String? = null,
    val name: String? = null,
    val principal: Double? = null,
    val currentBalance: Double? = null,
    val interestRate: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val currency: String? = null,
    val monthlyPayment: Double? = null,
    val payments: List<LiabilityPaymentDTO>? = null,
    val transactions: List<LiabilityTransactionDTO>? = null,
    val bankName: String? = null,
    val category: String? = null,
    val creditLimit: Double? = null,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val minPaymentPercentage: Double? = null,
    val lateFee: Double? = null,
    val interestType: String? = null,
    val status: String? = null,
    val isPaid: Boolean? = null,
    val description: String? = null,
    val availableCredit: Double? = null,
    val monthlyInterestAmount: Double? = null,
    val daysRemaining: Int? = null,
    val isOverdue: Boolean? = null,
    val totalInterestPaid: Double? = null,
    val totalPrincipalPaid: Double? = null,
    val monthlyInstallment: Double? = null,
    val tenor: Int? = null,
    val remainingTenor: Int? = null,
    val gracePeriodMonths: Int? = null,
    val remainingInstallment: Double? = null,
    val installmentPlans: List<LiabilityInlineInstallmentPlanDTO>? = null,
    val statements: List<LiabilityInlineStatementDTO>? = null
) {
    fun toDomain(): Liability {
        val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val parsedStartDate = startDate?.let {
            try { simpleDateFormatter.parse(it) } catch (e: Exception) { null }
        } ?: Date()

        val parsedEndDate = endDate?.let {
            try { simpleDateFormatter.parse(it) } catch (e: Exception) { null }
        } ?: Date()

        return Liability(
            id = id ?: "",
            name = name ?: "",
            principal = principal ?: 0.0,
            currentBalance = currentBalance ?: 0.0,
            interestRate = interestRate ?: 0.0,
            startDate = parsedStartDate,
            endDate = parsedEndDate,
            currency = currency,
            monthlyPayment = monthlyPayment,
            payments = payments?.map { it.toDomain() },
            transactions = transactions?.map { it.toDomain() },
            bankName = bankName,
            category = LiabilityCategory.fromValue(category ?: ""),
            creditLimit = creditLimit,
            billingDay = billingDay,
            dueDay = dueDay,
            minPaymentPercentage = minPaymentPercentage,
            lateFee = lateFee,
            interestType = interestType?.let { InterestType.fromValue(it) },
            status = status,
            isPaid = isPaid ?: false,
            description = description,
            availableCredit = availableCredit,
            monthlyInterestAmount = monthlyInterestAmount,
            daysRemaining = daysRemaining,
            isOverdue = isOverdue ?: false,
            totalInterestPaid = totalInterestPaid,
            totalPrincipalPaid = totalPrincipalPaid,
            monthlyInstallment = monthlyInstallment,
            tenor = tenor,
            remainingTenor = remainingTenor,
            remainingInstallment = remainingInstallment?.let { if (it > 0) it else null },
            installmentPlans = installmentPlans?.map { it.toDomain() }
        )
    }
}

data class SimulatePayoffResponseDTO(
    val totalMonthsToPayOff: Int,
    val estimatedPayoffDate: String,
    val totalPaymentAmount: Double,
    val interestSaved: Double,
    val monthsSaved: Int,
    val recommendation: String,
    val liabilityBreakdown: List<LiabilityBreakdownDTO>?
) {
    fun toDomain(): com.casha.app.domain.model.SimulatePayoffResponse {
        return com.casha.app.domain.model.SimulatePayoffResponse(
            totalMonthsToPayOff = totalMonthsToPayOff,
            estimatedPayoffDate = estimatedPayoffDate,
            totalPaymentAmount = totalPaymentAmount,
            interestSaved = interestSaved,
            monthsSaved = monthsSaved,
            recommendation = recommendation,
            liabilityBreakdown = liabilityBreakdown?.map { it.toDomain() } ?: emptyList()
        )
    }
}

data class LiabilityBreakdownDTO(
    val id: String? = null,
    val name: String,
    val currentBalance: Double,
    val monthlyPayment: Double,
    val monthsToPayOff: Int,
    val totalInterest: Double
) {
    fun toDomain(): com.casha.app.domain.model.LiabilityBreakdown {
        return com.casha.app.domain.model.LiabilityBreakdown(
            id = id,
            name = name,
            currentBalance = currentBalance,
            monthlyPayment = monthlyPayment,
            monthsToPayOff = monthsToPayOff,
            totalInterest = totalInterest
        )
    }
}
