package com.casha.app.domain.repository

import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetTransaction
import com.casha.app.domain.model.CreateAssetRequest
import com.casha.app.domain.model.CreateAssetTransactionRequest
import com.casha.app.domain.model.PortfolioSummary
import com.casha.app.domain.model.UpdateAssetRequest

interface PortfolioRepository {
    /**
     * Create a new asset
     */
    suspend fun createAsset(request: CreateAssetRequest): Asset

    /**
     * Get all assets for the current user
     */
    suspend fun getAssets(): List<Asset>

    /**
     * Get portfolio summary with assets calculation
     */
    suspend fun getPortfolioSummary(): PortfolioSummary

    /**
     * Update asset amount and name
     */
    suspend fun updateAsset(id: String, request: UpdateAssetRequest): Asset

    /**
     * Delete an asset
     */
    suspend fun deleteAsset(id: String)

    /**
     * Add asset transaction (buy/sell)
     */
    suspend fun addAssetTransaction(assetId: String, request: CreateAssetTransactionRequest): AssetTransaction

    /**
     * Get asset transactions history
     */
    suspend fun getAssetTransactions(assetId: String): List<AssetTransaction>
}
