package com.casha.app.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.casha.app.widget.data.SpendStatus

/**
 * Centralized theme system for all Casha widgets.
 * Provides consistent colors, typography, spacing, and shapes.
 * 
 * Based on Material Design 3 principles with Casha brand colors.
 */
object WidgetTheme {
    
    // ========== Brand Colors ==========
    
    /**
     * Primary brand green - used for comfortable status and success states.
     */
    val CashaGreen = Color(0xFF2E7D32)
    
    /**
     * Danger red - used for over budget and error states.
     */
    val CashaRed = Color(0xFFF44336)
    
    /**
     * Warning orange - used for caution status.
     */
    val CashaOrange = Color(0xFFFF9800)
    
    /**
     * Brand purple - used for balance ring and report actions.
     */
    val CashaPurple = Color(0xFF7F77DD)
    
    /**
     * Brand blue - used for AI chat action.
     */
    val CashaBlue = Color(0xFF3389E6)
    
    /**
     * Premium gold/yellow - used for premium upsell.
     */
    val CashaGold = Color(0xFFFFD700)
    
    // ========== Status Colors ==========
    
    /**
     * Get the appropriate color for a spend status.
     */
    fun getStatusColor(status: SpendStatus): Color = when (status) {
        SpendStatus.COMFORTABLE -> CashaGreen
        SpendStatus.CAUTION -> CashaOrange
        SpendStatus.OVER_BUDGET -> CashaRed
        SpendStatus.NO_INCOME -> Color.Gray
        SpendStatus.UNKNOWN -> Color.Gray
    }
    
    // ========== Text Colors ==========
    
    /**
     * Primary text color - highest emphasis.
     */
    val TextPrimary = Color.Black
    
    /**
     * Secondary text color - medium emphasis.
     */
    val TextSecondary = Color.Black.copy(alpha = 0.7f)
    
    /**
     * Tertiary text color - low emphasis.
     */
    val TextTertiary = Color.Black.copy(alpha = 0.5f)
    
    /**
     * Text color on colored backgrounds.
     */
    val TextOnColor = Color.White
    
    // ========== Background Colors ==========
    
    /**
     * Light background color for widget containers.
     */
    val BackgroundLight = Color(0xFFF8F8F8)
    
    /**
     * Card background color.
     */
    val BackgroundCard = Color.White
    
    /**
     * Subtle gradient end color.
     */
    val BackgroundGradientEnd = Color(0xFFF5F5F5)
    
    /**
     * Progress bar track color (unfilled portion).
     */
    val ProgressTrack = Color(0xFFE0E0E0)
    
    // ========== Action Button Colors ==========
    
    /**
     * AI Chat button color.
     */
    val ActionAI = CashaBlue
    
    /**
     * Report button color.
     */
    val ActionReport = CashaPurple
    
    /**
     * Budget button color.
     */
    val ActionBudget = CashaGreen
    
    // ========== Spacing ==========
    
    /**
     * Extra small spacing (4dp).
     */
    val SpacingXSmall: Dp = 4.dp
    
    /**
     * Small spacing (8dp).
     */
    val SpacingSmall: Dp = 8.dp
    
    /**
     * Medium spacing (12dp).
     */
    val SpacingMedium: Dp = 12.dp
    
    /**
     * Large spacing (16dp).
     */
    val SpacingLarge: Dp = 16.dp
    
    /**
     * Extra large spacing (24dp).
     */
    val SpacingXLarge: Dp = 24.dp
    
    // ========== Corner Radius ==========
    
    /**
     * Small corner radius for buttons and small elements (8dp).
     */
    val CornerRadiusSmall: Dp = 8.dp
    
    /**
     * Medium corner radius for cards and medium elements (12dp).
     */
    val CornerRadiusMedium: Dp = 12.dp
    
    /**
     * Large corner radius for widget containers (16dp).
     */
    val CornerRadiusLarge: Dp = 16.dp
    
    /**
     * Full circular corner radius.
     */
    val CornerRadiusFull: Dp = 999.dp
    
    // ========== Sizes ==========
    
    /**
     * Progress bar height (6dp for better visibility).
     */
    val ProgressBarHeight: Dp = 6.dp
    
    /**
     * Thin progress bar height for compact spaces (4dp).
     */
    val ProgressBarHeightThin: Dp = 4.dp
    
    /**
     * Lock screen progress bar height (5dp).
     */
    val ProgressBarHeightLock: Dp = 5.dp
    
    /**
     * Status badge circle size (9dp).
     */
    val StatusBadgeSize: Dp = 9.dp
    
    /**
     * Budget ring stroke width (6dp).
     */
    val RingStrokeWidth: Dp = 6.dp
    
    /**
     * Small budget ring size (48dp) - for medium widget.
     */
    val BudgetRingSmall: Dp = 48.dp
    
    /**
     * Medium budget ring size (64dp) - for large widget.
     */
    val BudgetRingMedium: Dp = 64.dp
    
    /**
     * Lock screen circular widget size (52dp).
     */
    val LockCircularSize: Dp = 52.dp
    
    /**
     * Lock screen circular inner circle size (40dp).
     */
    val LockCircularInnerSize: Dp = 40.dp
    
    /**
     * Action button height (40dp).
     */
    val ActionButtonHeight: Dp = 40.dp
    
    /**
     * Icon size for action buttons (20dp).
     */
    val ActionIconSize: Dp = 20.dp
    
    /**
     * Large icon size for fallback states (24dp).
     */
    val FallbackIconSize: Dp = 24.dp
    
    // ========== Helper Functions ==========
    
    /**
     * Get progress bar color based on progress percentage.
     * Green < 80%, Orange < 100%, Red >= 100%
     */
    fun getProgressColor(progress: Float): Color = when {
        progress < 0.8f -> CashaGreen
        progress < 1.0f -> CashaOrange
        else -> CashaRed
    }
    
    /**
     * Get a lighter version of a color (20% opacity).
     * Used for progress bar tracks and badge backgrounds.
     */
    fun Color.lighter(): Color = this.copy(alpha = 0.2f)
    
    /**
     * Get a semi-transparent version of a color (15% opacity).
     * Used for subtle backgrounds.
     */
    fun Color.subtle(): Color = this.copy(alpha = 0.15f)
}
