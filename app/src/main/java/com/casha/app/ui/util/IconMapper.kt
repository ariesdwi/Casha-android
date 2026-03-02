package com.casha.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps SwiftUI SF Symbol names to Android Material Icons.
 */
fun mapSFSymbolToImageVector(sfSymbol: String): ImageVector {
    return when (sfSymbol) {
        // Liquid Asset Icons
        "banknote" -> Icons.Default.Money
        "building.columns" -> Icons.Default.AccountBalance
        "creditcard" -> Icons.Default.CreditCard
        "lock.shield" -> Icons.Default.Security
        "drop.fill" -> Icons.Default.WaterDrop

        // Investment Icons
        "chart.line.uptrend.xyaxis" -> Icons.Default.TrendingUp
        "chart.pie" -> Icons.Default.PieChart
        "chart.bar.xaxis" -> Icons.Default.BarChart
        "doc.text" -> Icons.Default.Description
        "doc.text.fill" -> Icons.Default.Description
        "star.circle" -> Icons.Default.Stars

        // Commodities
        "circle.hexagongrid.fill" -> Icons.Default.Toll
        "circle.hexagongrid" -> Icons.Outlined.Toll

        // Crypto
        "bitcoinsign.circle" -> Icons.Default.CurrencyBitcoin
        "bitcoinsign.circle.fill" -> Icons.Default.CurrencyBitcoin

        // Real Estate
        "map" -> Icons.Default.Map
        "house" -> Icons.Default.Home
        "house.fill" -> Icons.Default.Home
        "building.2" -> Icons.Default.LocationCity

        // Vehicles
        "car" -> Icons.Default.DirectionsCar
        "car.fill" -> Icons.Default.DirectionsCar
        "bicycle" -> Icons.Default.PedalBike
        "car.2" -> Icons.Default.Commute

        // Business
        "briefcase" -> Icons.Default.BusinessCenter
        "briefcase.fill" -> Icons.Default.BusinessCenter

        // Others
        "shield" -> Icons.Default.Shield
        "person.crop.circle.badge.clock" -> Icons.Default.PendingActions
        "arrow.right.circle" -> Icons.Default.ArrowCircleRight
        "ellipsis.circle" -> Icons.Default.MoreHoriz
        "ellipsis.circle.fill" -> Icons.Default.MoreHoriz

        // Default fallback (e.g. general item)
        else -> Icons.Default.Category
    }
}
