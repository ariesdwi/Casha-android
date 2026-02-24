package com.casha.app.data.remote.dto

import com.casha.app.domain.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class LiabilityListDTO(
    val liabilities: List<LiabilityDto>? = null,
    val totalCount: Int? = null,
    val page: Int? = null,
    val pageSize: Int? = null
)

@Serializable
data class CategoryBreakdownDTO(
    val category: String? = null,
    val totalBalance: Double? = null,
    val monthlyInterest: Double? = null,
    val loansCount: Int? = null
) {
    fun toDomain(): CategoryBreakdown {
        return CategoryBreakdown(
            category = category ?: "UNKNOWN",
            totalBalance = totalBalance ?: 0.0,
            monthlyInterest = monthlyInterest ?: 0.0,
            loansCount = loansCount ?: 0
        )
    }
}

@Serializable
data class LiabilitySummaryDTO(
    val totalDebt: Double? = null,
    val totalPrincipal: Double? = null,
    val totalCurrentBalance: Double? = null,
    val totalMonthlyPayment: Double? = null,
    val totalMonthlyInterest: Double? = null,
    val totalYearlyInterest: Double? = null,
    val averageInterestRate: Double? = null,
    val activeLoansCount: Int? = null,
    val overdueLoansCount: Int? = null,
    val nextPaymentDue: String? = null,
    val totalInterestPaid: Double? = null,
    val categoryBreakdown: List<CategoryBreakdownDTO>? = null,
    val currency: String? = null
) {
    fun toDomain(): LiabilitySummary {
        return LiabilitySummary(
            totalDebt = totalDebt ?: 0.0,
            totalPrincipal = totalPrincipal ?: 0.0,
            totalCurrentBalance = totalCurrentBalance ?: 0.0,
            totalMonthlyPayment = totalMonthlyPayment ?: 0.0,
            totalMonthlyInterest = totalMonthlyInterest ?: 0.0,
            totalYearlyInterest = totalYearlyInterest ?: 0.0,
            averageInterestRate = averageInterestRate ?: 0.0,
            activeLoansCount = activeLoansCount ?: 0,
            overdueLoansCount = overdueLoansCount ?: 0,
            nextPaymentDue = nextPaymentDue,
            totalInterestPaid = totalInterestPaid ?: 0.0,
            categoryBreakdown = categoryBreakdown?.map { it.toDomain() } ?: emptyList(),
            currency = currency
        )
    }
}

@Serializable
data class LiabilityPaymentDTO(
    val id: String? = null,
    val liabilityId: String? = null,
    val loanName: String? = null,
    val amount: Double? = null,
    val principalPaid: Double? = null,
    val interestPaid: Double? = null,
    val datetime: String? = null,
    val paymentDate: String? = null,
    val paymentType: String? = null,
    val balanceAfterPayment: Double? = null,
    val status: String? = null,
    val createdAt: String? = null
) {
    fun toDomain(): LiabilityPayment {
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val isoDate = datetime?.let { try { isoFormatter.parse(it) } catch(e:Exception){ null } } 
            ?: createdAt?.let { try { isoFormatter.parse(it) } catch(e:Exception){ null } }
            ?: Date()

        return LiabilityPayment(
            id = id ?: "",
            liabilityId = liabilityId ?: "",
            loanName = loanName,
            amount = amount ?: 0.0,
            principalPaid = principalPaid,
            interestPaid = interestPaid,
            datetime = isoDate,
            paymentDate = paymentDate,
            paymentType = paymentType?.let { PaymentType.fromValue(it) },
            balanceAfterPayment = balanceAfterPayment,
            status = status,
            createdAt = createdAt?.let { try { isoFormatter.parse(it) } catch(e:Exception){ null } }
        )
    }
}

@Serializable
data class TransactionCategoryDTO(
    val id: String? = null,
    val name: String? = null
) {
    fun toDomain(): TransactionCategory {
        return TransactionCategory(
            id = id ?: "",
            name = name ?: ""
        )
    }
}

@Serializable
data class TransactionMetaDTO(
    val newBalance: Double? = null,
    val availableCredit: Double? = null
) {
    fun toDomain(): TransactionMeta {
        return TransactionMeta(
            newBalance = newBalance ?: 0.0,
            availableCredit = availableCredit ?: 0.0
        )
    }
}

@Serializable
data class LiabilityTransactionDTO(
    val id: String? = null,
    val name: String? = null,
    val amount: Double? = null,
    val datetime: String? = null,
    val currency: String? = null,
    val liabilityId: String? = null,
    val billingStatementId: String? = null,
    val category: JsonElement? = null,
    val meta: TransactionMetaDTO? = null,
    val type: String? = null
) {
    fun toDomain(): LiabilityTransaction {
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val parsedDate = datetime?.let { try { isoFormatter.parse(it) } catch (e: Exception) { null } } ?: Date()
        
        var mappedCategory: TransactionCategory? = null
        category?.let { catJson ->
            try {
                if (catJson.jsonObject.containsKey("id") || catJson.jsonObject.containsKey("name")) {
                    val id = catJson.jsonObject["id"]?.jsonPrimitive?.content ?: ""
                    val name = catJson.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                    mappedCategory = TransactionCategory(id, name)
                }
            } catch (e: Exception) {
                try {
                    val name = catJson.jsonPrimitive.content
                    mappedCategory = TransactionCategory("", name)
                } catch (e: Exception) {}
            }
        }

        return LiabilityTransaction(
            id = id ?: "",
            liabilityId = liabilityId ?: "",
            name = name ?: "",
            amount = amount ?: 0.0,
            currency = currency ?: com.casha.app.core.util.CurrencyFormatter.defaultCurrency,
            datetime = parsedDate,
            billingStatementId = billingStatementId,
            category = mappedCategory,
            meta = meta?.toDomain(),
            type = type
        )
    }
}

@Serializable
data class StatementPeriodDTO(
    val start: String? = null,
    val end: String? = null,
    val dueDate: String? = null
)

@Serializable
data class StatementSummaryDTO(
    val previousBalance: Double? = null,
    val purchasesMade: Double? = null,
    val interestCharged: Double? = null,
    val paymentsMade: Double? = null,
    val lateFeesCharged: Double? = null,
    val statementBalance: Double? = null,
    val minimumPayment: Double? = null
)

@Serializable
data class LiabilityStatementDTO(
    val id: String? = null,
    val liabilityId: String? = null,
    val period: StatementPeriodDTO? = null,
    val summary: StatementSummaryDTO? = null,
    val status: String? = null,
    val previousBalance: Double? = null,
    val purchasesMade: Double? = null,
    val interestCharged: Double? = null,
    val lateFeesCharged: Double? = null,
    val transactions: List<LiabilityTransactionDTO>? = null,
    val reminders: List<String>? = null,
    val warnings: List<String>? = null
) {
    fun toDomain(): LiabilityStatement {
        val df = SimpleDateFormat("dd MMM yyyy", Locale.US)
        
        val startDate = period?.start?.let { try { df.parse(it) } catch(e:Exception){ null } } ?: Date()
        val endDate = period?.end?.let { try { df.parse(it) } catch(e:Exception){ null } } ?: Date()
        val dueDateParams = period?.dueDate?.let { try { df.parse(it) } catch(e:Exception){ null } } ?: Date()
        
        return LiabilityStatement(
            id = id ?: "",
            liabilityId = liabilityId ?: "",
            startDate = startDate,
            endDate = endDate,
            dueDate = dueDateParams,
            statementBalance = summary?.statementBalance ?: 0.0,
            minimumPayment = summary?.minimumPayment ?: 0.0,
            status = StatementStatus.fromValue(status ?: ""),
            paymentsMade = summary?.paymentsMade ?: 0.0,
            previousBalance = summary?.previousBalance ?: previousBalance ?: 0.0,
            purchasesMade = summary?.purchasesMade ?: purchasesMade ?: 0.0,
            interestCharged = summary?.interestCharged ?: interestCharged ?: 0.0,
            lateFeesCharged = summary?.lateFeesCharged ?: lateFeesCharged ?: 0.0,
            transactions = transactions?.map { it.toDomain() } ?: emptyList(),
            reminders = reminders ?: emptyList(),
            warnings = warnings ?: emptyList()
        )
    }
    
    companion object {
        fun fromDetailData(data: StatementDetailDataDTO, liabilityId: String): LiabilityStatement {
            val df = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val st = data.statement
            val period = st?.period
            
            val startDate = period?.start?.let { try { df.parse(it) } catch(e:Exception){ null } } ?: Date()
            val endDate = period?.end?.let { try { df.parse(it) } catch(e:Exception){ null } } ?: Date()
            val dueDateParams = period?.dueDate?.let { try { df.parse(it) } catch(e:Exception){ null } } ?: Date()
            
            val warningList = data.meta?.warning?.let { listOf(it) } ?: emptyList()
            
            return LiabilityStatement(
                id = st?.id ?: "",
                liabilityId = liabilityId,
                startDate = startDate,
                endDate = endDate,
                dueDate = dueDateParams,
                statementBalance = st?.statementBalance ?: 0.0,
                minimumPayment = st?.minimumPayment ?: 0.0,
                status = StatementStatus.fromValue(data.meta?.status ?: ""),
                paymentsMade = st?.paymentsMade ?: 0.0,
                previousBalance = st?.previousBalance ?: 0.0,
                purchasesMade = st?.purchasesMade ?: 0.0,
                interestCharged = st?.interestCharged ?: 0.0,
                lateFeesCharged = st?.lateFeesCharged ?: 0.0,
                transactions = data.transactions?.map { it.toDomain() } ?: emptyList(),
                reminders = emptyList(),
                warnings = warningList
            )
        }
    }
}

@Serializable
data class SimpleTransactionDTO(
    val date: String? = null,
    val name: String? = null,
    val amount: Double? = null,
    val type: String? = null
) {
    fun toDomain(): LiabilityTransaction {
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val transactionDate = date?.let { try { dateFormatter.parse(it) } catch(e:Exception){ null }} ?: Date()
        val transactionAmount = Math.abs(amount ?: 0.0)
        
        return LiabilityTransaction(
            id = java.util.UUID.randomUUID().toString(),
            name = name ?: "Unknown",
            amount = transactionAmount,
            datetime = transactionDate,
            currency = com.casha.app.core.util.CurrencyFormatter.defaultCurrency,
            liabilityId = "",
            billingStatementId = null,
            category = null,
            meta = null,
            type = type
        )
    }
}

@Serializable
data class StatementMetaDTO(
    val status: String? = null,
    val warning: String? = null
)

@Serializable
data class StatementInfoDTO(
    val id: String? = null,
    val period: StatementPeriodDTO? = null,
    val previousBalance: Double? = null,
    val purchasesMade: Double? = null,
    val interestCharged: Double? = null,
    val paymentsMade: Double? = null,
    val lateFeesCharged: Double? = null,
    val statementBalance: Double? = null,
    val minimumPayment: Double? = null
)

@Serializable
data class LiabilityStatementsResponseDTO(
    val status: String? = null,
    val statements: List<LiabilityStatementDTO>? = null
)

@Serializable
data class StatementDetailDataDTO(
    val status: String? = null,
    val statement: StatementInfoDTO? = null,
    val transactions: List<SimpleTransactionDTO>? = null,
    val meta: StatementMetaDTO? = null
)

@Serializable
data class UnbilledTransactionsDTO(
    val currentUsage: Double? = null,
    val previousUnpaidBalance: Double? = null,
    val projectedInterest: Double? = null,
    val projectedLateFees: Double? = null,
    val totalProjectedStatement: Double? = null,
    val transactionCount: Int? = null,
    val transactions: List<LiabilityTransactionDTO>? = null
) {
    fun toDomain(): UnbilledTransactions {
        return UnbilledTransactions(
            currentUsage = currentUsage ?: 0.0,
            previousUnpaidBalance = previousUnpaidBalance ?: 0.0,
            projectedInterest = projectedInterest ?: 0.0,
            projectedLateFees = projectedLateFees ?: 0.0,
            totalProjectedStatement = totalProjectedStatement ?: 0.0,
            transactionCount = transactionCount ?: 0,
            transactions = transactions?.map { it.toDomain() } ?: emptyList()
        )
    }
}

@Serializable
data class PredictionMessageDTO(
    val type: String? = null,
    val message: String? = null,
    val amount: Double? = null
) {
    fun toDomain(): PredictionMessage {
        return PredictionMessage(
            type = type ?: "UNKNOWN",
            message = message ?: "",
            amount = amount ?: 0.0
        )
    }
}

@Serializable
data class LiabilityInsightDTO(
    val totalInterestPaid: Double? = null,
    val totalLateFeesPaid: Double? = null,
    val projectedInterestIfMinimum: Double? = null,
    val savingsIfPaidInFull: Double? = null,
    val predictionMessages: List<PredictionMessageDTO>? = null
) {
    fun toDomain(): LiabilityInsight {
        return LiabilityInsight(
            totalInterestPaid = totalInterestPaid ?: 0.0,
            totalLateFeesPaid = totalLateFeesPaid ?: 0.0,
            projectedInterestIfMinimum = projectedInterestIfMinimum ?: 0.0,
            savingsIfPaidInFull = savingsIfPaidInFull ?: 0.0,
            predictionMessages = predictionMessages?.map { it.toDomain() } ?: emptyList()
        )
    }
}

@Serializable
data class LiabilityPaymentHistoryDTO(
    val payments: List<LiabilityPaymentDTO>? = null,
    val totalAmount: Double? = null,
    val totalInterest: Double? = null,
    val totalCount: Int? = null
) {
    fun toDomain(): LiabilityPaymentHistory {
        return LiabilityPaymentHistory(
            payments = payments?.map { it.toDomain() } ?: emptyList(),
            totalAmount = totalAmount ?: 0.0,
            totalInterest = totalInterest,
            totalCount = totalCount ?: 0
        )
    }
}

@Serializable
data class InstallmentPlanDTO(
    val id: String? = null,
    val liabilityId: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val totalAmount: Double? = null,
    val monthlyAmount: Double? = null,
    val tenor: Int? = null,
    val currentMonth: Int? = null,
    val isActive: Boolean? = null,
    val startDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): InstallmentPlan {
        val sysFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        return InstallmentPlan(
            id = id ?: "",
            liabilityId = liabilityId ?: "",
            userId = userId ?: "",
            name = name ?: "",
            totalAmount = totalAmount ?: 0.0,
            monthlyAmount = monthlyAmount ?: 0.0,
            tenor = tenor ?: 0,
            currentMonth = currentMonth ?: 0,
            isActive = isActive ?: false,
            startDate = startDate?.let { try { sysFormat.parse(it) } catch(e:Exception){ null } } ?: Date(),
            createdAt = createdAt?.let { try { sysFormat.parse(it) } catch(e:Exception){ null } } ?: Date(),
            updatedAt = updatedAt?.let { try { sysFormat.parse(it) } catch(e:Exception){ null } } ?: Date()
        )
    }
}
