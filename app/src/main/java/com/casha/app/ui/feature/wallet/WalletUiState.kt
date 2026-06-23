package com.casha.app.ui.feature.wallet

import com.casha.app.domain.model.Wallet
import com.casha.app.domain.model.WalletSource
import com.casha.app.domain.model.WalletSummary

data class WalletUiState(
    val wallets: List<Wallet> = emptyList(),
    val summary: WalletSummary? = null,
    val defaultWalletId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val liquidWallets: List<Wallet>
        get() = wallets.filter { it.source == WalletSource.ASSET }

    val creditWallets: List<Wallet>
        get() = wallets.filter { it.source == WalletSource.LOAN }
}
