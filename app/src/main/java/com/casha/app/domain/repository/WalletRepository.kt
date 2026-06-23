package com.casha.app.domain.repository

import com.casha.app.domain.model.*

interface WalletRepository {
    suspend fun getWallets(): List<Wallet>
    suspend fun getWalletSummary(): WalletSummary
    suspend fun setDefaultWallet(walletId: String)
    suspend fun clearDefaultWallet()
    suspend fun addLiquidWallet(request: AddLiquidWalletRequest): Wallet
    suspend fun addCreditCard(request: AddCreditCardRequest): Wallet
    suspend fun updateWallet(id: String, source: WalletSource, request: UpdateWalletRequest): Wallet
    suspend fun deleteWallet(id: String, source: WalletSource)
    suspend fun transferWallet(request: TransferWalletRequest): TransferWalletResult
}
