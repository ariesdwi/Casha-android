package com.casha.app.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.casha.app.widget.WidgetTheme
import com.casha.app.widget.data.*
import com.casha.app.widget.util.WidgetCurrencyFormatter
import com.casha.app.widget.util.WidgetLogger

/**
 * Home Screen Small Widget (2×2) - Modern Material Design 3
 * 
 * Features:
 * - Larger, more readable text (28sp for amounts)
 * - Thicker progress bar (6dp)
 * - Better spacing (12dp padding)
 * - Status badge with enhanced visibility
 * - Gradient-ready background
 * - WidgetTheme integration
 */
class HomeSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetLogger.logRender("HomeSmallWidget", "provideGlance")
            HomeSmallContent(context)
        }
    }
}

class HomeSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeSmallWidget()
}

@Composable
private fun HomeSmallContent(context: Context) {
    val (state, summary) = resolveWidgetState(context)
    
    WidgetLogger.logRender("HomeSmallWidget", state.name)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetTheme.CornerRadiusLarge) // 16dp
            .background(WidgetTheme.BackgroundCard) // White
            .padding(WidgetTheme.SpacingMedium) // 12dp
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://add-expense")
                )
            )
    ) {
        when (state) {
            WidgetState.LOGGED_OUT -> WidgetFallbackView("👤", "Login ke Casha", "Buka app untuk masuk")
            WidgetState.NOT_PREMIUM -> WidgetFallbackView("👑", "Casha Premium", "Upgrade untuk widget")
            WidgetState.NO_DATA -> WidgetFallbackView("🔄", "Buka Casha", "Menunggu data")
            WidgetState.HIDDEN -> WidgetFallbackView("🔒", "Saldo Tersembunyi", "••••")
            WidgetState.NORMAL -> SmallNormalContent(summary!!)
        }
    }
}

/**
 * Normal content for HomeSmallWidget with modern design.
 * 
 * Layout:
 * - Header: "⚡ SAFE SPEND" + larger status badge
 * - Amount: Larger (28sp), bold, prominent
 * - Spent today: Secondary text + thicker progress bar (6dp)
 * - Footer: Status badge + days remaining
 * 
 * Improvements:
 * - 28sp amount (was 20sp)
 * - 6dp progress bar (was 4dp)
 * - 12dp padding (was 14dp, now consistent)
 * - 9dp status badge (was 8dp)
 * - Better spacing using WidgetTheme
 */
@Composable
private fun SmallNormalContent(summary: WidgetSummary) {
    val statusColor = WidgetTheme.getStatusColor(summary.spendStatus)

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // Header: Safe Spend label + Status badge
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "⚡ SAFE SPEND",
                style = TextStyle(
                    fontSize = 10.sp, // Slightly larger (was 9sp)
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetTheme.TextSecondary) // 70% opacity
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            // Larger status badge
            Box(
                modifier = GlanceModifier
                    .size(WidgetTheme.StatusBadgeSize) // 9dp (was 8dp)
                    .cornerRadius(WidgetTheme.StatusBadgeSize / 2)
                    .background(statusColor)
            ) {}
        }

        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingSmall)) // 8dp

        // Amount: LARGER and more prominent
        Text(
            text = WidgetCurrencyFormatter.formatFull(summary.safeSpendToday, summary.currency),
            style = TextStyle(
                fontSize = 28.sp, // INCREASED from 20sp
                fontWeight = FontWeight.Bold,
                color = ColorProvider(WidgetTheme.TextPrimary) // Black
            )
        )

        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingSmall)) // 8dp

        // Spent today + Progress bar
        if (summary.spentToday > 0 && summary.safeSpendToday > 0) {
            Text(
                text = "Spent: ${WidgetCurrencyFormatter.formatShort(summary.spentToday, summary.currency)}",
                style = TextStyle(
                    fontSize = 11.sp, // Slightly larger (was 10sp)
                    color = ColorProvider(WidgetTheme.TextTertiary) // 50% opacity
                )
            )
            Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp
            
            // THICKER progress bar
            WidgetProgressBar(
                progress = (summary.spentToday / summary.safeSpendToday).coerceIn(0.0, 1.0).toFloat(),
                summary = summary
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Footer: Status label + Days remaining
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Status badge with background
            Box(
                modifier = GlanceModifier
                    .cornerRadius(WidgetTheme.CornerRadiusSmall) // 8dp
                    .background(statusColor.copy(alpha = 0.15f)) // Subtle background
                    .padding(horizontal = 8.dp, vertical = 3.dp) // Slightly larger padding
            ) {
                Text(
                    text = summary.statusLabel,
                    style = TextStyle(
                        fontSize = 10.sp, // Slightly larger (was 9sp)
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(statusColor)
                    )
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${summary.daysRemaining} hari",
                style = TextStyle(
                    fontSize = 10.sp, // Slightly larger (was 9sp)
                    color = ColorProvider(WidgetTheme.TextTertiary) // 50% opacity
                )
            )
        }
    }
}

/**
 * Progress bar component with improved visibility.
 * 
 * Improvements:
 * - 6dp height (was 4dp) - MUCH more visible
 * - Uses WidgetTheme colors
 * - Smooth color transitions based on progress
 * - Rounded ends for modern look
 */
@Composable
private fun WidgetProgressBar(progress: Float, summary: WidgetSummary) {
    val barColor = WidgetTheme.getProgressColor(progress)
    
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(WidgetTheme.ProgressBarHeight) // 6dp (was 4dp)
            .cornerRadius(WidgetTheme.ProgressBarHeight / 2) // Fully rounded ends
            .background(WidgetTheme.ProgressTrack) // Light gray
    ) {
        // Use weighted row to simulate fractional width
        Row(modifier = GlanceModifier.fillMaxSize()) {
            if (progress > 0.01f) {
                Box(
                    modifier = GlanceModifier
                        .height(WidgetTheme.ProgressBarHeight)
                        .defaultWeight()
                        .cornerRadius(WidgetTheme.ProgressBarHeight / 2)
                        .background(barColor)
                ) {}
            }
            if (progress < 0.99f) {
                Spacer(
                    modifier = GlanceModifier
                        .height(WidgetTheme.ProgressBarHeight)
                        .defaultWeight()
                )
            }
        }
    }
}

/**
 * Fallback view for non-normal states.
 * Shows centered icon with title and subtitle.
 * 
 * Improvements:
 * - Larger icon (32sp, was 28sp)
 * - Better spacing using WidgetTheme
 * - Uses theme colors
 */
@Composable
fun WidgetFallbackView(icon: String, title: String, subtitle: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://dashboard")
                )
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = TextStyle(fontSize = 32.sp) // Larger (was 28sp)
        )
        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingSmall)) // 8dp
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp, // Slightly larger (was 13sp)
                fontWeight = FontWeight.Bold,
                color = ColorProvider(WidgetTheme.TextPrimary)
            )
        )
        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp
        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = 12.sp, // Slightly larger (was 11sp)
                color = ColorProvider(WidgetTheme.TextSecondary)
            )
        )
        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingSmall)) // 8dp
        Text(
            text = "Tap untuk buka app",
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(WidgetTheme.TextTertiary)
            )
        )
    }
}
