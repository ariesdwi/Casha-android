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
 * Lock screen rectangular widget showing budget bar (Android 14+).
 * 
 * Modern Design Features:
 * - Larger text (17sp for amounts, was 14sp)
 * - Thicker progress bar (5dp, was 3dp)
 * - Better spacing (8dp between rows)
 * - Material Design 3 styling
 * - WidgetTheme integration
 * - Professional appearance for lock screen viewing distance
 */
class LockRectangularWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetLogger.logRender("LockRectangularWidget", "provideGlance")
            LockRectangularContent(context)
        }
    }
}

class LockRectangularWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LockRectangularWidget()
}

/**
 * Rectangular lock widget content with modern design.
 * 
 * Improvements:
 * - Uses WidgetTheme throughout
 * - Better spacing (8dp between rows)
 * - Larger text for lock screen viewing
 * - Thicker progress bar (5dp)
 * - Professional styling
 */
@Composable
private fun LockRectangularContent(context: Context) {
    val (state, summary) = resolveWidgetState(context)
    
    WidgetLogger.logRender("LockRectangularWidget", state.name)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetTheme.CornerRadiusMedium) // 12dp
            .background(WidgetTheme.BackgroundLight) // Light gray
            .padding(horizontal = WidgetTheme.SpacingMedium, vertical = WidgetTheme.SpacingSmall) // 12dp, 8dp
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://budget")
                )
            )
    ) {
        when (state) {
            WidgetState.LOGGED_OUT -> LockFallback("👤", "Belum login", "Buka Casha untuk mulai")
            WidgetState.NOT_PREMIUM -> LockFallback("👑", "Casha Premium", "Upgrade untuk akses widget")
            WidgetState.NO_DATA -> LockFallback("🔄", "Menunggu data", "Buka app untuk sinkronisasi")
            WidgetState.HIDDEN -> LockFallback("🔒", "Balance hidden", "••••")
            WidgetState.NORMAL -> LockNormalContent(summary!!)
        }
    }
}

/**
 * Normal content for lock rectangular widget with modern design.
 * 
 * Layout:
 * - Row 1: "SAFE SPEND TODAY" + status badge (right aligned)
 * - Row 2: Amount (LARGE - 17sp)
 * - Row 3: Progress bar (THICKER - 5dp)
 * - Row 4: Budget % + days remaining
 * 
 * Improvements:
 * - 17sp amount (was 14sp) - 21% larger!
 * - 5dp progress bar (was 3dp) - 67% thicker!
 * - 8dp spacing between rows (was 2-3dp)
 * - All WidgetTheme colors and sizes
 * - Better readability from lock screen distance
 */
@Composable
private fun LockNormalContent(summary: WidgetSummary) {
    val statusColor = WidgetTheme.getStatusColor(summary.spendStatus)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        // Row 1: "SAFE SPEND TODAY" + status badge
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "SAFE SPEND TODAY",
                style = TextStyle(
                    fontSize = 9.sp, // Slightly larger (was 8sp)
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetTheme.TextSecondary)
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Box(
                modifier = GlanceModifier
                    .cornerRadius(WidgetTheme.CornerRadiusSmall) // 8dp
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = summary.statusLabel,
                    style = TextStyle(
                        fontSize = 8.sp, // Slightly larger (was 7sp)
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(statusColor)
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp (was 2dp)

        // Row 2: Amount - LARGER for lock screen viewing
        Text(
            text = WidgetCurrencyFormatter.formatFull(summary.safeSpendToday, summary.currency),
            style = TextStyle(
                fontSize = 17.sp, // MUCH LARGER (was 14sp) - 21% increase!
                fontWeight = FontWeight.Bold,
                color = ColorProvider(WidgetTheme.TextPrimary) // Black
            )
        )

        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp (was 3dp)

        // Row 3: Progress bar - THICKER
        val progress = if (summary.safeSpendToday > 0) {
            (summary.spentToday / summary.safeSpendToday).coerceIn(0.0, 1.0).toFloat()
        } else 0f
        val barColor = WidgetTheme.getProgressColor(progress)
        
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(WidgetTheme.ProgressBarHeightLock) // 5dp (was 3dp) - 67% thicker!
                .cornerRadius(WidgetTheme.ProgressBarHeightLock / 2) // Fully rounded ends
                .background(WidgetTheme.ProgressTrack) // Light gray
        ) {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                if (progress > 0.01f) {
                    Box(
                        modifier = GlanceModifier
                            .height(WidgetTheme.ProgressBarHeightLock)
                            .defaultWeight()
                            .cornerRadius(WidgetTheme.ProgressBarHeightLock / 2)
                            .background(barColor)
                    ) {
                        // Empty progress fill
                    }
                }
                if (progress < 0.99f) {
                    Spacer(
                        modifier = GlanceModifier
                            .height(WidgetTheme.ProgressBarHeightLock)
                            .defaultWeight()
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp (was 2dp)

        // Row 4: Footer text - better formatting
        Text(
            text = "${summary.budgetPctUsed}% budget • ${summary.daysRemaining} hari lagi",
            style = TextStyle(
                fontSize = 9.sp, // Slightly larger (was 8sp)
                color = ColorProvider(WidgetTheme.TextTertiary) // 50% opacity
            )
        )
    }
}

/**
 * Fallback view for lock rectangular widget with modern design.
 * 
 * Improvements:
 * - Larger icon (22sp, was 20sp)
 * - Better text sizes (12sp title, 10sp subtitle)
 * - Uses WidgetTheme colors
 * - Better spacing
 */
@Composable
private fun LockFallback(icon: String, title: String, subtitle: String) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = icon,
            style = TextStyle(fontSize = 22.sp) // Larger (was 20sp)
        )
        Spacer(modifier = GlanceModifier.width(WidgetTheme.SpacingSmall)) // 8dp
        Column {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 12.sp, // Larger (was 11sp)
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetTheme.TextPrimary)
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 10.sp, // Larger (was 9sp)
                    color = ColorProvider(WidgetTheme.TextSecondary)
                )
            )
        }
    }
}
