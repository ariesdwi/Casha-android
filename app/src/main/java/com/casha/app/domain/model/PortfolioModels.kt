package com.casha.app.domain.model

import java.util.Date

// MARK: - Asset Model
data class Asset(
    val id: String,
    val name: String,
    val type: AssetType,
    val amount: Double,
    val currency: String,
    val description: String? = null,
    val userId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val incomeEntries: List<IncomeCasha>? = null,
    
    // Quantity-based tracking
    val quantity: Double? = null,
    val unit: String? = null,
    val pricePerUnit: Double? = null,
    
    // Additional fields
    val acquisitionDate: Date? = null,
    val location: String? = null
)

// MARK: - Asset Type (28 Types)
enum class AssetType(val rawValue: String) {
    // Liquid Assets
    CASH("CASH"),
    SAVINGS_ACCOUNT("SAVINGS_ACCOUNT"),
    CHECKING_ACCOUNT("CHECKING_ACCOUNT"),
    DEPOSIT("DEPOSIT"),
    
    // Investments - Equity
    STOCK("STOCK"),
    MUTUAL_FUND("MUTUAL_FUND"),
    ETF("ETF"),
    
    // Investments - Fixed Income
    BOND_GOVERNMENT("BOND_GOVERNMENT"),
    BOND_CORPORATE("BOND_CORPORATE"),
    SUKUK("SUKUK"),
    
    // Commodities
    GOLD_PHYSICAL("GOLD_PHYSICAL"),
    GOLD_DIGITAL("GOLD_DIGITAL"),
    SILVER_PHYSICAL("SILVER_PHYSICAL"),
    
    // Crypto
    CRYPTOCURRENCY("CRYPTOCURRENCY"),
    
    // Real Estate
    LAND("LAND"),
    RESIDENTIAL_PROPERTY("RESIDENTIAL_PROPERTY"),
    COMMERCIAL_PROPERTY("COMMERCIAL_PROPERTY"),
    
    // Vehicles
    VEHICLE_CAR("VEHICLE_CAR"),
    VEHICLE_MOTORCYCLE("VEHICLE_MOTORCYCLE"),
    VEHICLE_OTHER("VEHICLE_OTHER"),
    
    // Business
    BUSINESS_OWNERSHIP("BUSINESS_OWNERSHIP"),
    
    // Others
    INSURANCE_CASH_VALUE("INSURANCE_CASH_VALUE"),
    PENSION_FUND("PENSION_FUND"),
    RECEIVABLES("RECEIVABLES"),
    OTHER("OTHER");

    val displayName: String
        get() = when (this) {
            // Liquid
            CASH -> "Cash"
            SAVINGS_ACCOUNT -> "Savings Account"
            CHECKING_ACCOUNT -> "Checking Account"
            DEPOSIT -> "Deposit"
            // Equity
            STOCK -> "Stock"
            MUTUAL_FUND -> "Mutual Fund"
            ETF -> "ETF"
            // Fixed Income
            BOND_GOVERNMENT -> "Government Bond"
            BOND_CORPORATE -> "Corporate Bond"
            SUKUK -> "Sukuk"
            // Commodities
            GOLD_PHYSICAL -> "Gold (Physical)"
            GOLD_DIGITAL -> "Gold (Digital)"
            SILVER_PHYSICAL -> "Silver (Physical)"
            // Crypto
            CRYPTOCURRENCY -> "Cryptocurrency"
            // Real Estate
            LAND -> "Land"
            RESIDENTIAL_PROPERTY -> "Residential Property"
            COMMERCIAL_PROPERTY -> "Commercial Property"
            // Vehicles
            VEHICLE_CAR -> "Car"
            VEHICLE_MOTORCYCLE -> "Motorcycle"
            VEHICLE_OTHER -> "Other Vehicle"
            // Business
            BUSINESS_OWNERSHIP -> "Business Ownership"
            // Others
            INSURANCE_CASH_VALUE -> "Insurance Cash Value"
            PENSION_FUND -> "Pension Fund"
            RECEIVABLES -> "Receivables"
            OTHER -> "Other"
        }

    val icon: String
        get() = when (this) {
            // Liquid
            CASH -> "banknote"
            SAVINGS_ACCOUNT -> "building.columns"
            CHECKING_ACCOUNT -> "creditcard"
            DEPOSIT -> "lock.shield"
            // Equity
            STOCK -> "chart.line.uptrend.xyaxis"
            MUTUAL_FUND -> "chart.pie"
            ETF -> "chart.bar.xaxis"
            // Fixed Income
            BOND_GOVERNMENT, BOND_CORPORATE -> "doc.text"
            SUKUK -> "star.circle"
            // Commodities
            GOLD_PHYSICAL, GOLD_DIGITAL -> "circle.hexagongrid.fill"
            SILVER_PHYSICAL -> "circle.hexagongrid"
            // Crypto
            CRYPTOCURRENCY -> "bitcoinsign.circle"
            // Real Estate
            LAND -> "map"
            RESIDENTIAL_PROPERTY -> "house"
            COMMERCIAL_PROPERTY -> "building.2"
            // Vehicles
            VEHICLE_CAR -> "car"
            VEHICLE_MOTORCYCLE -> "bicycle"
            VEHICLE_OTHER -> "car.2"
            // Business
            BUSINESS_OWNERSHIP -> "briefcase"
            // Others
            INSURANCE_CASH_VALUE -> "shield"
            PENSION_FUND -> "person.crop.circle.badge.clock"
            RECEIVABLES -> "arrow.right.circle"
            OTHER -> "ellipsis.circle"
        }

    // Recommended unit for quantity-based assets
    val recommendedUnit: String?
        get() = when (this) {
            GOLD_PHYSICAL, GOLD_DIGITAL, SILVER_PHYSICAL -> "gram"
            STOCK -> "lot"
            MUTUAL_FUND, ETF -> "unit"
            CRYPTOCURRENCY -> "coin"
            LAND -> "m²"
            BOND_GOVERNMENT, BOND_CORPORATE, SUKUK -> "lembar"
            RESIDENTIAL_PROPERTY, COMMERCIAL_PROPERTY, VEHICLE_CAR, VEHICLE_MOTORCYCLE, VEHICLE_OTHER -> "unit"
            BUSINESS_OWNERSHIP -> "%"
            else -> null
        }

    // Check if asset type typically uses quantity-based tracking
    val isQuantityBased: Boolean
        get() = when (this) {
            STOCK, MUTUAL_FUND, ETF, BOND_GOVERNMENT, BOND_CORPORATE, SUKUK,
            GOLD_PHYSICAL, GOLD_DIGITAL, SILVER_PHYSICAL, CRYPTOCURRENCY, LAND -> true
            else -> false
        }

    // Category for grouping in UI
    val category: AssetCategory
        get() = when (this) {
            CASH, SAVINGS_ACCOUNT, CHECKING_ACCOUNT, DEPOSIT -> AssetCategory.LIQUID
            STOCK, MUTUAL_FUND, ETF -> AssetCategory.EQUITY
            BOND_GOVERNMENT, BOND_CORPORATE, SUKUK -> AssetCategory.FIXED_INCOME
            GOLD_PHYSICAL, GOLD_DIGITAL, SILVER_PHYSICAL -> AssetCategory.COMMODITIES
            CRYPTOCURRENCY -> AssetCategory.CRYPTO
            LAND, RESIDENTIAL_PROPERTY, COMMERCIAL_PROPERTY -> AssetCategory.REAL_ESTATE
            VEHICLE_CAR, VEHICLE_MOTORCYCLE, VEHICLE_OTHER -> AssetCategory.VEHICLES
            BUSINESS_OWNERSHIP -> AssetCategory.BUSINESS
            INSURANCE_CASH_VALUE, PENSION_FUND, RECEIVABLES, OTHER -> AssetCategory.OTHERS
        }
}

// MARK: - Asset Category
enum class AssetCategory(val rawValue: String) {
    LIQUID("Liquid Assets"),
    EQUITY("Equity Investments"),
    FIXED_INCOME("Fixed Income"),
    COMMODITIES("Commodities"),
    CRYPTO("Cryptocurrency"),
    REAL_ESTATE("Real Estate"),
    VEHICLES("Vehicles"),
    BUSINESS("Business"),
    OTHERS("Others");

    val icon: String
        get() = when (this) {
            LIQUID -> "drop.fill"
            EQUITY -> "chart.line.uptrend.xyaxis"
            FIXED_INCOME -> "doc.text.fill"
            COMMODITIES -> "circle.hexagongrid.fill"
            CRYPTO -> "bitcoinsign.circle.fill"
            REAL_ESTATE -> "house.fill"
            VEHICLES -> "car.fill"
            BUSINESS -> "briefcase.fill"
            OTHERS -> "ellipsis.circle.fill"
        }

    val assetTypes: List<AssetType>
        get() = AssetType.entries.filter { it.category == this }
}

// MARK: - Create Asset Request
data class CreateAssetRequest(
    val name: String,
    val type: AssetType,
    val amount: Double? = null,
    val currency: String? = null,
    val description: String? = null,
    // Quantity-based fields
    val quantity: Double? = null,
    val unit: String? = null,
    val pricePerUnit: Double? = null,
    // Additional fields
    val acquisitionDate: Date? = null,
    val location: String? = null
)

// MARK: - Update Asset Request
data class UpdateAssetRequest(
    val amount: Double? = null,
    val name: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val pricePerUnit: Double? = null,
    val acquisitionDate: Date? = null,
    val location: String? = null,
    val description: String? = null
)

// MARK: - Asset Transaction Type
enum class AssetTransactionType(val rawValue: String) {
    SAVING("SAVING"),
    WITHDRAW("WITHDRAW");

    val displayName: String
        get() = when (this) {
            SAVING -> "Saving"
            WITHDRAW -> "Withdraw"
        }

    val icon: String
        get() = when (this) {
            SAVING -> "arrow.down.circle.fill"
            WITHDRAW -> "arrow.up.circle.fill"
        }

    val color: String
        get() = when (this) {
            SAVING -> "green"
            WITHDRAW -> "red"
        }
}

// MARK: - Asset Transaction
data class AssetTransaction(
    val id: String,
    val assetId: String,
    val type: AssetTransactionType,
    val quantity: Double? = null,
    val pricePerUnit: Double? = null,
    val totalAmount: Double,
    val datetime: Date,
    val note: String? = null,
    val costBasis: Double? = null,
    val profitLoss: Double? = null,
    val createdAt: Date
)

// MARK: - Create Asset Transaction Request
data class CreateAssetTransactionRequest(
    val type: AssetTransactionType,
    val quantity: Double? = null,
    val pricePerUnit: Double? = null,
    val amount: Double? = null,
    val datetime: Date? = null,
    val note: String? = null
)

// MARK: - Portfolio Summary
data class PortfolioSummary(
    val currency: String,
    val totalAssets: Double,
    val breakdown: List<AssetBreakdown>,
    val assets: List<Asset>,
    val lastUpdated: Date
)

// MARK: - Asset Breakdown
data class AssetBreakdown(
    val type: AssetType,
    val amount: Double,
    val count: Int
) {
    val percentage: Double
        get() = 0.0 // Will be calculated based on total assets where needed
}
