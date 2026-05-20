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

class HomeSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(Color.White)
            .padding(14.dp)
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

@Composable
private fun SmallNormalContent(summary: WidgetSummary) {
    val statusColor = summary.spendStatus.color

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "⚡ SAFE SPEND",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF666666))
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .cornerRadius(4.dp)
                    .background(statusColor)
            ) {}
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Amount
        Text(
            text = WidgetCurrencyFormatter.formatFull(summary.safeSpendToday, summary.currency),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.Black)
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Spent today
        if (summary.spentToday > 0 && summary.safeSpendToday > 0) {
            Text(
                text = "Spent: ${WidgetCurrencyFormatter.formatShort(summary.spentToday, summary.currency)}",
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF888888)))
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            WidgetProgressBar(
                progress = (summary.spentToday / summary.safeSpendToday).coerceIn(0.0, 1.0).toFloat(),
                summary = summary
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Footer
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
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
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${summary.daysRemaining} hari",
                style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color(0xFF888888)))
            )
        }
    }
}

@Composable
private fun WidgetProgressBar(progress: Float, summary: WidgetSummary) {
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
        // Use weighted row to simulate fractional width
        Row(modifier = GlanceModifier.fillMaxSize()) {
            if (progress > 0.01f) {
                Box(
                    modifier = GlanceModifier
                        .height(4.dp)
                        .defaultWeight()
                        .cornerRadius(2.dp)
                        .background(barColor)
                ) {}
            }
            if (progress < 0.99f) {
                Spacer(
                    modifier = GlanceModifier
                        .height(4.dp)
                        .defaultWeight()
                )
            }
        }
    }
}

@Composable
fun WidgetFallbackView(icon: String, title: String, subtitle: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(text = icon, style = TextStyle(fontSize = 28.sp))
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = title,
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.Black))
        )
        Text(
            text = subtitle,
            style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color(0xFF888888)))
        )
    }
}
