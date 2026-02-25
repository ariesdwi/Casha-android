package com.casha.app.domain.model

import java.util.Date

enum class LiabilityCategory(val rawValue: String) {
    MORTGAGE("MORTGAGE"),
    PERSONAL_LOAN("PERSONAL_LOAN"),
    AUTO_LOAN("AUTO_LOAN"),
    STUDENT_LOAN("STUDENT_LOAN"),
    BUSINESS_LOAN("BUSINESS_LOAN"),
    CREDIT_CARD("CREDIT_CARD"),
    PAY_LATER("PAY_LATER"),
    OTHER("OTHER");

    val displayName: String
        get() = when (this) {
            MORTGAGE -> "Mortgage (KPR)"
            PERSONAL_LOAN -> "Personal Loan"
            AUTO_LOAN -> "Auto Loan"
            STUDENT_LOAN -> "Student Loan"
            BUSINESS_LOAN -> "Business Loan"
            CREDIT_CARD -> "Credit Card"
            PAY_LATER -> "Pay Later"
            OTHER -> "Other"
        }

    val isCreditCard: Boolean
        get() = this == CREDIT_CARD

    val isRevolving: Boolean
        get() = this == CREDIT_CARD || this == PAY_LATER

    val icon: String
        get() = when (this) {
            MORTGAGE -> "house.fill"
            PERSONAL_LOAN -> "person.fill"
            AUTO_LOAN -> "car.fill"
            STUDENT_LOAN -> "graduationcap.fill"
            BUSINESS_LOAN -> "briefcase.fill"
            CREDIT_CARD -> "creditcard.fill"
            PAY_LATER -> "clock.arrow.2.circlepath"
            OTHER -> "questionmark.circle.fill"
        }

    companion object {
        fun fromValue(value: String): LiabilityCategory {
            return entries.find { it.rawValue == value } ?: OTHER
        }
    }
}

enum class InterestType(val rawValue: String) {
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY"),
    FLAT("FLAT");

    val displaySuffix: String
        get() = when (this) {
            MONTHLY -> "/mo"
            YEARLY -> "/yr"
            FLAT -> ""
        }

    companion object {
        fun fromValue(value: String): InterestType {
            return entries.find { it.rawValue == value } ?: FLAT
        }
    }
}

enum class PaymentType(val rawValue: String) {
    FULL("FULL"),
    PARTIAL("PARTIAL"),
    MINIMUM("MINIMUM"),
    EXTRA("EXTRA");

    companion object {
        fun fromValue(value: String): PaymentType? {
            return entries.find { it.rawValue == value }
        }
    }
}

enum class StatementStatus(val rawValue: String) {
    OPEN("OPEN"),
    PAID("PAID"),
    PARTIAL("PARTIAL"),
    LATE("LATE");

    companion object {
        fun fromValue(value: String): StatementStatus {
            return entries.find { it.rawValue == value } ?: OPEN
        }
    }
}

data class TransactionCategory(
    val id: String,
    val name: String
)

data class TransactionMeta(
    val newBalance: Double,
    val availableCredit: Double
)

data class Liability(
    val id: String,
    val name: String,
    val principal: Double,
    val currentBalance: Double,
    val interestRate: Double,
    val startDate: Date,
    val endDate: Date,
    val currency: String? = null,
    val monthlyPayment: Double? = null,
    val payments: List<LiabilityPayment>? = null,
    val transactions: List<LiabilityTransaction>? = null,
    val bankName: String? = null,
    val category: LiabilityCategory = LiabilityCategory.MORTGAGE,
    val creditLimit: Double? = null,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val minPaymentPercentage: Double? = null,
    val lateFee: Double? = null,
    val interestType: InterestType? = null,
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
    val remainingInstallment: Double? = null,
    val installmentPlans: List<InstallmentPlan>? = null
)

// ── Requests ──

data class CreateLiabilityRequest(
    val name: String,
    val bankName: String? = null,
    val category: LiabilityCategory = LiabilityCategory.MORTGAGE,
    val creditLimit: Double? = null,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val minPaymentPercentage: Double? = null,
    val interestRate: Double,
    val interestType: InterestType? = null,
    val lateFee: Double? = null,
    val principal: Double,
    val currentBalance: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val description: String? = null,
    val tenor: Int? = null,
    val monthlyInstallment: Double? = null,
    val gracePeriodMonths: Int? = null
)

data class CreateLiabilityPaymentRequest(
    val amount: Double,
    val paymentDate: String,
    val paymentType: PaymentType? = null,
    val principalAmount: Double? = null,
    val interestAmount: Double? = null,
    val notes: String? = null
)

data class CreateLiabilityInstallmentRequest(
    val name: String,
    val totalAmount: Double,
    val monthlyAmount: Double,
    val tenor: Int,
    val currentMonth: Int,
    val startDate: String
)

data class CreateLiabilityTransactionRequest(
    val name: String,
    val amount: Double,
    val categoryId: String,
    val datetime: String,
    val description: String? = null
)

// ── Additional Domain Models ──

data class CategoryBreakdown(
    val category: String,
    val totalBalance: Double,
    val monthlyInterest: Double,
    val loansCount: Int
) {
    val displayCategory: String
        get() = LiabilityCategory.fromValue(category).displayName
}

data class LiabilitySummary(
    val totalDebt: Double,
    val totalPrincipal: Double = 0.0,
    val totalCurrentBalance: Double = 0.0,
    val totalMonthlyPayment: Double,
    val totalMonthlyInterest: Double = 0.0,
    val totalYearlyInterest: Double = 0.0,
    val averageInterestRate: Double = 0.0,
    val activeLoansCount: Int,
    val overdueLoansCount: Int = 0,
    val nextPaymentDue: String? = null,
    val totalInterestPaid: Double = 0.0,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val currency: String? = null
)

data class LiabilityPayment(
    val id: String,
    val liabilityId: String,
    val loanName: String? = null,
    val amount: Double,
    val principalPaid: Double? = null,
    val interestPaid: Double? = null,
    val datetime: Date,
    val paymentDate: String? = null,
    val paymentType: PaymentType? = null,
    val balanceAfterPayment: Double? = null,
    val status: String? = null,
    val createdAt: Date? = null
)

data class LiabilityTransaction(
    val id: String,
    val name: String,
    val amount: Double,
    val datetime: Date,
    val currency: String,
    val liabilityId: String,
    val billingStatementId: String? = null,
    val category: TransactionCategory? = null,
    val meta: TransactionMeta? = null,
    val type: String? = null
) {
    fun toCashaTransaction(): com.casha.app.domain.model.TransactionCasha {
        return com.casha.app.domain.model.TransactionCasha(
            id = id,
            name = name,
            category = category?.name ?: "Other",
            amount = amount,
            datetime = datetime,
            isSynced = true,
            liabilityId = liabilityId,
            createdAt = datetime,
            updatedAt = datetime
        )
    }
}

enum class SimulationStrategy(val rawValue: String, val displayName: String) {
    AVALANCHE("AVALANCHE", "Suku Bunga Tertinggi (Avalanche)"),
    SNOWBALL("SNOWBALL", "Saldo Terkecil (Snowball)"),
    CUSTOM("CUSTOM", "Kustom");

    companion object {
        fun fromValue(value: String): SimulationStrategy {
            return entries.find { it.rawValue == value } ?: AVALANCHE
        }
    }
}

data class SimulatePayoffRequest(
    val strategy: SimulationStrategy,
    val additionalPayment: Double? = null
)

data class SimulatePayoffResponse(
    val totalMonthsToPayOff: Int,
    val estimatedPayoffDate: String,
    val totalPaymentAmount: Double,
    val interestSaved: Double,
    val monthsSaved: Int,
    val recommendation: String,
    val liabilityBreakdown: List<LiabilityBreakdown>
)

data class LiabilityBreakdown(
    val id: String? = null,
    val name: String,
    val currentBalance: Double,
    val monthlyPayment: Double,
    val monthsToPayOff: Int,
    val totalInterest: Double
)

data class LiabilityStatement(
    val id: String,
    val liabilityId: String,
    val startDate: Date,
    val endDate: Date,
    val dueDate: Date,
    val statementBalance: Double,
    val minimumPayment: Double,
    val status: StatementStatus,
    val paymentsMade: Double,
    val previousBalance: Double,
    val purchasesMade: Double,
    val interestCharged: Double,
    val lateFeesCharged: Double,
    val transactions: List<LiabilityTransaction>,
    val reminders: List<String>,
    val warnings: List<String>
)

data class UnbilledTransactions(
    val currentUsage: Double,
    val previousUnpaidBalance: Double,
    val projectedInterest: Double,
    val projectedLateFees: Double,
    val totalProjectedStatement: Double,
    val transactionCount: Int,
    val transactions: List<LiabilityTransaction>
)

data class PredictionMessage(
    val type: String,
    val message: String,
    val amount: Double
)

data class LiabilityInsight(
    val totalInterestPaid: Double,
    val totalLateFeesPaid: Double,
    val projectedInterestIfMinimum: Double,
    val savingsIfPaidInFull: Double,
    val predictionMessages: List<PredictionMessage>
)

data class LiabilityPaymentHistory(
    val payments: List<LiabilityPayment>,
    val totalAmount: Double,
    val totalInterest: Double? = null,
    val totalCount: Int
)

data class InstallmentPlan(
    val id: String,
    val liabilityId: String,
    val userId: String,
    val name: String,
    val totalAmount: Double,
    val monthlyAmount: Double,
    val tenor: Int,
    val currentMonth: Int,
    val isActive: Boolean,
    val progress: Int? = null,
    val remainingAmount: Double? = null,
    val remainingMonths: Int? = null,
    val startDate: Date,
    val createdAt: Date,
    val updatedAt: Date
)
