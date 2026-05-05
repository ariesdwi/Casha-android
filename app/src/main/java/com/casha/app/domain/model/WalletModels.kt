package com.casha.app.domain.model

import kotlin.math.max

data class Wallet(
    val id: String,
    val source: WalletSource,
    val name: String,
    val type: WalletType,
    val balance: Double,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val bankName: String?,
    val creditLimit: Double?,
    val billingDay: Int?,
    val dueDay: Int?,
    val minimumPayment: Double?
) {
    val availableCredit: Double?
        get() = if (source == WalletSource.LOAN && creditLimit != null) max(0.0, creditLimit - balance) else null
}

enum class WalletSource { ASSET, LOAN }

enum class WalletType { CASH, SAVINGS_ACCOUNT, CHECKING_ACCOUNT, E_WALLET, CREDIT_CARD, OTHER }

data class WalletSummary(
    val liquidBalance: Double,
    val totalCreditUsed: Double,
    val totalCreditLimit: Double,
    val availableCredit: Double,
    val walletCount: Int
)

data class TransferWalletResult(
    val from: Wallet,
    val to: Wallet
)

data class AddLiquidWalletRequest(
    val name: String,
    val type: WalletType,
    val amount: Double,
    val description: String? = null,
    val bankName: String? = null
)

data class AddCreditCardRequest(
    val name: String,
    val bankName: String,
    val creditLimit: Double,
    val currentBalance: Double,
    val interestRate: Double,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val interestType: String? = null,
    val minPaymentPercentage: Double? = null,
    val lateFee: Double? = null
)

data class UpdateWalletRequest(
    val name: String? = null,
    val amount: Double? = null
)

data class TransferWalletRequest(
    val fromWalletId: String,
    val toWalletId: String,
    val amount: Double,
    val note: String? = null
)
