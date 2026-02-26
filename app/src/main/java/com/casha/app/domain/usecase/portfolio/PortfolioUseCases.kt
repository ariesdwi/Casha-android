package com.casha.app.domain.usecase.portfolio

import com.casha.app.domain.model.*
import com.casha.app.domain.repository.PortfolioRepository
import javax.inject.Inject

// MARK: - Portfolio Errors
sealed class PortfolioException(message: String) : Exception(message) {
    object InvalidAmount : PortfolioException("Amount must be greater than 0")
    object InvalidName : PortfolioException("Asset name cannot be empty")
    object InvalidQuantity : PortfolioException("Quantity and price must be greater than 0")
    object AssetNotFound : PortfolioException("Asset not found")
}

class AddAssetTransactionUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(assetId: String, request: CreateAssetTransactionRequest): AssetTransaction {
        return repository.addAssetTransaction(assetId, request)
    }
}

// MARK: - Create Asset Use Case
class CreateAssetUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(request: CreateAssetRequest): Asset {
        // Validate name is not empty
        if (request.name.trim().isEmpty()) {
            throw PortfolioException.InvalidName
        }
        
        // Validate based on asset type
        if (request.type.isQuantityBased) {
            // Validate quantity and price per unit
            val quantity = request.quantity
            val price = request.pricePerUnit
            if (quantity == null || quantity <= 0 || price == null || price <= 0) {
                throw PortfolioException.InvalidQuantity
            }
        } else {
            // Validate direct amount
            val amount = request.amount
            if (amount == null || amount <= 0) {
                throw PortfolioException.InvalidAmount
            }
        }
        
        return repository.createAsset(request)
    }
}

class DeleteAssetUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(id: String): Asset {
        return repository.deleteAsset(id)
    }
}

class GetAssetsUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(): List<Asset> {
        return repository.getAssets()
    }
}

class GetAssetTransactionsUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(assetId: String): List<AssetTransaction> {
        return repository.getAssetTransactions(assetId)
    }
}

// MARK: - Get Portfolio Summary Use Case
class GetPortfolioSummaryUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(): PortfolioSummary {
        return repository.getPortfolioSummary()
    }
}

class UpdateAssetUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend fun execute(id: String, request: UpdateAssetRequest): Asset {
        return repository.updateAsset(id = id, request = request)
    }
}
