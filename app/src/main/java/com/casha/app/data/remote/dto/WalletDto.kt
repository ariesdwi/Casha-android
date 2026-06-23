package com.casha.app.data.remote.dto

import com.casha.app.domain.model.Wallet
import com.casha.app.domain.model.WalletSource
import com.casha.app.domain.model.WalletSummary
import com.casha.app.domain.model.WalletType
import kotlinx.serialization.Serializable

@Serializable
data class WalletDto(
    val id: String? = null,
    val source: String? = null,
    val name: String? = null,
    val type: String? = null,
    val balance: Double? = null,
    val description: String? = null,
    val bankName: String? = null,
    val creditLimit: Double? = null,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val minimumPayment: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Wallet {
        return Wallet(
            id = id ?: "",
            source = when (source?.uppercase()) {
                "LOAN" -> WalletSource.LOAN
                else -> WalletSource.ASSET
            },
            name = name ?: "",
            type = type?.let {
                try { WalletType.valueOf(it.uppercase()) } catch (e: Exception) { WalletType.OTHER }
            } ?: WalletType.OTHER,
            balance = balance ?: 0.0,
            description = description,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: "",
            bankName = bankName,
            creditLimit = creditLimit,
            billingDay = billingDay,
            dueDay = dueDay,
            minimumPayment = minimumPayment
        )
    }
}

@Serializable
data class WalletSummaryDto(
    val liquidBalance: Double? = null,
    val totalCreditUsed: Double? = null,
    val totalCreditLimit: Double? = null,
    val availableCredit: Double? = null,
    val walletCount: Int? = null
) {
    fun toDomain(): WalletSummary {
        return WalletSummary(
            liquidBalance = liquidBalance ?: 0.0,
            totalCreditUsed = totalCreditUsed ?: 0.0,
            totalCreditLimit = totalCreditLimit ?: 0.0,
            availableCredit = availableCredit ?: 0.0,
            walletCount = walletCount ?: 0
        )
    }
}

@Serializable
data class TransferWalletResponseDto(
    val from: WalletDto? = null,
    val to: WalletDto? = null
)

@Serializable
data class SetDefaultWalletRequestDto(
    val walletId: String
)

@Serializable
data class TransferWalletRequestDto(
    val fromWalletId: String,
    val toWalletId: String,
    val amount: Double,
    val note: String? = null
)

@Serializable
data class AddLiquidWalletRequestDto(
    val name: String,
    val type: String,
    val amount: Double,
    val description: String? = null,
    val bankName: String? = null
)

@Serializable
data class AddCreditCardRequestDto(
    val name: String,
    val category: String = "CREDIT_CARD",
    val bankName: String,
    val creditLimit: Double,
    val currentBalance: Double,
    val interestRate: Double,
    val principal: Double = 0.0,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val interestType: String? = null,
    val minPaymentPercentage: Double? = null,
    val lateFee: Double? = null
)

@Serializable
data class UpdateWalletRequestDto(
    val name: String? = null,
    val amount: Double? = null
)
