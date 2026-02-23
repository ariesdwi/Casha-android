package com.casha.app.data.remote.dto

import com.casha.app.domain.model.*
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val totalPrincipalPaid: Double? = null
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
            totalPrincipalPaid = totalPrincipalPaid
        )
    }
}
