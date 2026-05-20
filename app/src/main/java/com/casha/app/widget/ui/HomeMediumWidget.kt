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
import com.casha.app.widget.data.*
import com.casha.app.widget.util.WidgetCurrencyFormatter

class HomeMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(Color.White)
            .padding(14.dp)
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

@Composable
private fun MediumNormalContent(summary: WidgetSummary) {
    val statusColor = summary.spendStatus.color

    Row(modifier = GlanceModifier.fillMaxSize()) {
        // Left column — Budget Summary
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .clickable(
                    actionRunCallback<DeepLinkAction>(
                        actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://budget")
                    )
                )
        ) {
            Text(
                text = "⚡ SAFE SPEND TODAY",
                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF666666)))
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = WidgetCurrencyFormatter.formatFull(summary.safeSpendToday, summary.currency),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.Black))
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            if (summary.spentToday > 0 && summary.safeSpendToday > 0) {
                Text(
                    text = "Spent: ${WidgetCurrencyFormatter.formatShort(summary.spentToday, summary.currency)}",
                    style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF888888)))
                )
                Spacer(modifier = GlanceModifier.height(3.dp))
                val progress = (summary.spentToday / summary.safeSpendToday).coerceIn(0.0, 1.0).toFloat()
                val barColor = when {
                    progress < 0.8f -> Color(0xFF2E7D32)
                    progress < 1.0f -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .cornerRadius(2.dp)
                        .background(Color(0xFFE0E0E0))
                ) {
                    Row(modifier = GlanceModifier.fillMaxSize()) {
                        if (progress > 0.01f) {
                            Box(
                                modifier = GlanceModifier.height(4.dp).defaultWeight().cornerRadius(2.dp).background(barColor)
                            ) {}
                        }
                        if (progress < 0.99f) {
                            Spacer(modifier = GlanceModifier.height(4.dp).defaultWeight())
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Budget ring + status
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                // Circular budget indicator
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(18.dp)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(28.dp)
                            .cornerRadius(14.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${summary.budgetPctUsed}%",
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorProvider(statusColor))
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.width(6.dp))
                Column {
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(4.dp)
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = summary.statusLabel,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = ColorProvider(statusColor))
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "${summary.daysRemaining} hari",
                        style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color(0xFF888888)))
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Right column — Quick Actions
        Column(
            modifier = GlanceModifier.width(44.dp).fillMaxHeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            QuickActionButton("✨", Color(0xFF3389E6), "casha://add-transaction")
            Spacer(modifier = GlanceModifier.height(8.dp))
            QuickActionButton("📊", Color(0xFF7F77DD), "casha://report")
            Spacer(modifier = GlanceModifier.height(8.dp))
            QuickActionButton("📈", Color(0xFF2E7D32), "casha://budget")
        }
    }
}

@Composable
private fun QuickActionButton(icon: String, color: Color, deepLink: String) {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .cornerRadius(20.dp)
            .background(color)
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to deepLink)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, style = TextStyle(fontSize = 16.sp))
    }
}
