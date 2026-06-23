package com.casha.app.domain.usecase.wallet

import com.casha.app.domain.model.*
import com.casha.app.domain.repository.WalletRepository
import javax.inject.Inject

class GetWalletsUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(): List<Wallet> {
        return repository.getWallets()
    }
}

class GetWalletSummaryUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(): WalletSummary {
        return repository.getWalletSummary()
    }
}

class SetDefaultWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(walletId: String) {
        repository.setDefaultWallet(walletId)
    }
}

class ClearDefaultWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute() {
        repository.clearDefaultWallet()
    }
}

class AddLiquidWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(request: AddLiquidWalletRequest): Wallet {
        if (request.name.trim().isEmpty()) {
            throw WalletException.InvalidName
        }
        if (request.amount < 0) {
            throw WalletException.InvalidAmount
        }
        return repository.addLiquidWallet(request)
    }
}

class AddCreditCardUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(request: AddCreditCardRequest): Wallet {
        if (request.name.trim().isEmpty()) {
            throw WalletException.InvalidName
        }
        if (request.creditLimit <= 0) {
            throw WalletException.InvalidAmount
        }
        if (request.bankName.trim().isEmpty()) {
            throw WalletException.InvalidName
        }
        return repository.addCreditCard(request)
    }
}

class UpdateWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(id: String, source: WalletSource, request: UpdateWalletRequest): Wallet {
        return repository.updateWallet(id, source, request)
    }
}

class DeleteWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(id: String, source: WalletSource) {
        repository.deleteWallet(id, source)
    }
}

class TransferWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend fun execute(request: TransferWalletRequest): TransferWalletResult {
        if (request.amount <= 0) {
            throw WalletException.InvalidAmount
        }
        if (request.fromWalletId == request.toWalletId) {
            throw WalletException.SameWallet
        }
        return repository.transferWallet(request)
    }
}

sealed class WalletException(message: String) : Exception(message) {
    object InvalidName : WalletException("Wallet name cannot be empty")
    object InvalidAmount : WalletException("Amount must be valid")
    object SameWallet : WalletException("Cannot transfer to the same wallet")
}
