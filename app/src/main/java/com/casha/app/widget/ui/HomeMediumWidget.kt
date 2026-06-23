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
 * Home Screen Medium Widget (4×2) - iOS-Style Design
 * 
 * Design based on iOS screenshot:
 * - Two-column layout (budget left, actions right)
 * - Budget ring with flat progress (not colored outer ring)
 * - Action buttons stacked vertically
 * - Status with colored dot + label
 */
class HomeMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetLogger.logRender("HomeMediumWidget", "provideGlance")
            HomeMediumContent(context)
        }
    }
}

class HomeMediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeMediumWidget()
}

@Composable
private fun HomeMediumContent(context: Context) {
    val (state, summary) = resolveWidgetState(context)
    
    WidgetLogger.logRender("HomeMediumWidget", state.name)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetTheme.CornerRadiusLarge) // 16dp
            .background(WidgetTheme.BackgroundCard) // White
            .padding(WidgetTheme.SpacingMedium) // 12dp
    ) {
        when (state) {
            WidgetState.LOGGED_OUT -> WidgetFallbackView("👤", "Login ke Casha", "Buka app untuk masuk")
            WidgetState.NOT_PREMIUM -> WidgetFallbackView("👑", "Casha Premium", "Upgrade untuk akses widget")
            WidgetState.NO_DATA -> WidgetFallbackView("🔄", "Buka Casha", "Menunggu data")
            WidgetState.HIDDEN -> WidgetFallbackView("🔒", "Saldo Tersembunyi", "••••")
            WidgetState.NORMAL -> MediumNormalContent(summary!!)
        }
    }
}

/**
 * Normal content for HomeMediumWidget - iOS-style layout.
 * 
 * Layout (Two Columns):
 * - Left: Budget info (safe spend, spent, ring, status)
 * - Right: 3 action buttons (stacked vertically)
 * 
 * Based on iOS design screenshot.
 */
@Composable
private fun MediumNormalContent(summary: WidgetSummary) {
    val statusColor = WidgetTheme.getStatusColor(summary.spendStatus)

    Row(modifier = GlanceModifier.fillMaxSize()) {
        // LEFT COLUMN: Budget Summary
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // Row 1: "⚡ SAFE SPEND TODAY" label only
            Text(
                text = "⚡ SAFE SPEND TODAY",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetTheme.TextSecondary)
                )
            )

            Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp

            // Row 2: Amount (large)
            Text(
                text = WidgetCurrencyFormatter.formatFull(summary.safeSpendToday, summary.currency),
                style = TextStyle(
                    fontSize = 22.sp, // Slightly smaller to fit
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetTheme.TextPrimary)
                )
            )

            Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp

            // Row 3: Spent today + Progress bar
            if (summary.spentToday > 0 && summary.safeSpendToday > 0) {
                Text(
                    text = "Spent today: ${WidgetCurrencyFormatter.formatShort(summary.spentToday, summary.currency)}",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(WidgetTheme.TextTertiary)
                    )
                )
                Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingXSmall)) // 4dp
                
                // Progress bar
                val progress = (summary.spentToday / summary.safeSpendToday).coerceIn(0.0, 1.0).toFloat()
                val barColor = WidgetTheme.getProgressColor(progress)
                
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(4.dp) // Thinner like iOS
                        .cornerRadius(2.dp)
                        .background(WidgetTheme.ProgressTrack)
                ) {
                    Row(modifier = GlanceModifier.fillMaxSize()) {
                        if (progress > 0.01f) {
                            Box(
                                modifier = GlanceModifier
                                    .height(4.dp)
                                    .defaultWeight()
                                    .cornerRadius(2.dp)
                                    .background(barColor)
                            ) {
                                // Empty progress fill
                            }
                        }
                        if (progress < 0.99f) {
                            Spacer(modifier = GlanceModifier.height(4.dp).defaultWeight())
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Row 4: Budget ring + Status info (iOS style)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Budget ring (like iOS)
                Box(
                    modifier = GlanceModifier
                        .size(64.dp)
                        .cornerRadius(32.dp)
                        .background(WidgetTheme.ProgressTrack) // Gray background
                        .clickable(
                            actionRunCallback<DeepLinkAction>(
                                actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://budget")
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring with progress
                    Box(
                        modifier = GlanceModifier
                            .size(64.dp)
                            .cornerRadius(32.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner content
                        Column(
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "BUDGET",
                                style = TextStyle(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(WidgetTheme.TextTertiary)
                                )
                            )
                            Text(
                                text = "${summary.budgetPctUsed}%",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(WidgetTheme.TextPrimary)
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = GlanceModifier.width(10.dp))
                
                // Status info (iOS style)
                Column {
                    // Status with colored dot
                    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                        Box(
                            modifier = GlanceModifier
                                .size(8.dp)
                                .cornerRadius(4.dp)
                                .background(statusColor)
                        ) {
                            // Empty dot
                        }
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = summary.statusLabel,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(WidgetTheme.TextPrimary)
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "${summary.daysRemaining} hari lagi",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(WidgetTheme.TextSecondary)
                        )
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // RIGHT COLUMN: Action Buttons (stacked vertically like iOS)
        Column(
            modifier = GlanceModifier.width(72.dp).fillMaxHeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            QuickActionButton("✨", WidgetTheme.ActionAI, "casha://add-transaction")
            Spacer(modifier = GlanceModifier.height(10.dp))
            QuickActionButton("📊", WidgetTheme.ActionReport, "casha://report")
            Spacer(modifier = GlanceModifier.height(10.dp))
            QuickActionButton("📈", WidgetTheme.ActionBudget, "casha://budget")
        }
    }
}

/**
 * Quick action button component - iOS style (rounded square).
 * 
 * Improvements:
 * - 56dp size (larger for better touch)
 * - Rounded square shape (12dp radius)
 * - Uses WidgetTheme colors
 * - Better accessibility
 */
@Composable
private fun QuickActionButton(icon: String, color: Color, deepLink: String) {
    Box(
        modifier = GlanceModifier
            .size(56.dp) // Larger (was 44dp)
            .cornerRadius(12.dp) // Rounded square (not circle)
            .background(color)
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to deepLink)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = TextStyle(fontSize = 20.sp) // Slightly larger
        )
    }
}
