package com.casha.app.data.remote.impl

import com.casha.app.core.network.NetworkError
import com.casha.app.core.network.safeApiCall
import com.casha.app.data.remote.api.WalletApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.WalletRepository
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val api: WalletApiService
) : WalletRepository {

    override suspend fun getWallets(): List<Wallet> {
        val result = safeApiCall { api.getWallets() }
        return result.fold(
            onSuccess = { response ->
                response.data?.map { it.toDomain() } ?: emptyList()
            },
            onFailure = { throw it }
        )
    }

    override suspend fun getWalletSummary(): WalletSummary {
        val result = safeApiCall { api.getWalletSummary() }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun setDefaultWallet(walletId: String) {
        val result = safeApiCall {
            api.setDefaultWallet(SetDefaultWalletRequestDto(walletId = walletId))
        }
        result.fold(
            onSuccess = { },
            onFailure = { throw it }
        )
    }

    override suspend fun clearDefaultWallet() {
        val result = safeApiCall { api.clearDefaultWallet() }
        result.fold(
            onSuccess = { },
            onFailure = { throw it }
        )
    }

    override suspend fun addLiquidWallet(request: AddLiquidWalletRequest): Wallet {
        val dto = AddLiquidWalletRequestDto(
            name = request.name,
            type = request.type.name,
            amount = request.amount,
            description = request.description,
            bankName = request.bankName
        )
        val result = safeApiCall { api.createLiquidWallet(dto) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun addCreditCard(request: AddCreditCardRequest): Wallet {
        val dto = AddCreditCardRequestDto(
            name = request.name,
            category = "CREDIT_CARD",
            bankName = request.bankName,
            creditLimit = request.creditLimit,
            currentBalance = request.currentBalance,
            interestRate = request.interestRate,
            principal = 0.0,
            billingDay = request.billingDay,
            dueDay = request.dueDay,
            interestType = request.interestType,
            minPaymentPercentage = request.minPaymentPercentage,
            lateFee = request.lateFee
        )
        val result = safeApiCall { api.createCreditCard(dto) }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun updateWallet(id: String, source: WalletSource, request: UpdateWalletRequest): Wallet {
        val dto = UpdateWalletRequestDto(
            name = request.name,
            amount = request.amount
        )
        val result = when (source) {
            WalletSource.ASSET -> safeApiCall { api.updateAssetWallet(id, dto) }
            WalletSource.LOAN -> safeApiCall { api.updateLoanWallet(id, dto) }
        }
        return result.fold(
            onSuccess = { response ->
                response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response")
            },
            onFailure = { throw it }
        )
    }

    override suspend fun deleteWallet(id: String, source: WalletSource) {
        val result = when (source) {
            WalletSource.ASSET -> safeApiCall { api.deleteAssetWallet(id) }
            WalletSource.LOAN -> safeApiCall { api.deleteLoanWallet(id) }
        }
        result.fold(
            onSuccess = { },
            onFailure = { throw it }
        )
    }

    override suspend fun transferWallet(request: TransferWalletRequest): TransferWalletResult {
        val dto = TransferWalletRequestDto(
            fromWalletId = request.fromWalletId,
            toWalletId = request.toWalletId,
            amount = request.amount,
            note = request.note
        )
        val result = safeApiCall { api.transferWallet(dto) }
        return result.fold(
            onSuccess = { response ->
                val data = response.data ?: throw NetworkError.RequestFailed("Invalid response")
                TransferWalletResult(
                    from = data.from?.toDomain() ?: throw NetworkError.RequestFailed("Invalid from wallet"),
                    to = data.to?.toDomain() ?: throw NetworkError.RequestFailed("Invalid to wallet")
                )
            },
            onFailure = { throw it }
        )
    }
}
