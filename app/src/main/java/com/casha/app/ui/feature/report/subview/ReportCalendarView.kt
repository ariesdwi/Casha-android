package com.casha.app.ui.feature.report.subview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.DailySpending
import com.casha.app.domain.model.MonthlySpending
import com.casha.app.domain.model.ReportFilterPeriod
import com.casha.app.ui.theme.CashaPrimaryLight
import com.casha.app.ui.theme.CashaPrimaryDark
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// Sunday-first, matching iOS design
private val DAY_HEADERS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun ReportCalendarView(
    selectedPeriod: ReportFilterPeriod,
    calendarDisplayMonth: YearMonth,
    calendarWeekStart: LocalDate,
    calendarDisplayYear: Int,
    dailySpending: List<DailySpending>,
    monthlySpending: List<MonthlySpending>,
    isLoading: Boolean,
    onNavigate: (Int) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onMonthTap: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLowest

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Navigation row ────────────────────────────────────────────
            if (selectedPeriod != ReportFilterPeriod.CUSTOM) {
                val navLabel = when (selectedPeriod) {
                    ReportFilterPeriod.MONTH -> "${calendarDisplayMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendarDisplayMonth.year}"
                    ReportFilterPeriod.WEEK  -> {
                        val end = calendarWeekStart.plusDays(6)
                        "${calendarWeekStart.dayOfMonth} – ${end.dayOfMonth} ${end.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${end.year}"
                    }
                    ReportFilterPeriod.YEAR  -> "$calendarDisplayYear"
                    else -> ""
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NavArrowButton(primaryColor = primaryColor, direction = -1, onClick = { onNavigate(-1) })
                    Text(
                        text = navLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    NavArrowButton(primaryColor = primaryColor, direction = 1, onClick = { onNavigate(1) })
                }
            }

            // ── Calendar grid ─────────────────────────────────────────────
            when (selectedPeriod) {
                ReportFilterPeriod.MONTH, ReportFilterPeriod.CUSTOM -> {
                    MonthGrid(
                        month = calendarDisplayMonth,
                        dailySpending = dailySpending,
                        isLoading = isLoading,
                        primaryColor = primaryColor,
                        onDayClick = onDayClick
                    )
                }
                ReportFilterPeriod.WEEK -> {
                    WeekRow(
                        weekStart = calendarWeekStart,
                        dailySpending = dailySpending,
                        isLoading = isLoading,
                        primaryColor = primaryColor,
                        onDayClick = onDayClick
                    )
                }
                ReportFilterPeriod.YEAR -> {
                    YearGrid(
                        year = calendarDisplayYear,
                        monthlySpending = monthlySpending,
                        primaryColor = primaryColor,
                        onMonthTap = onMonthTap
                    )
                }
            }

            // ── Hint text ─────────────────────────────────────────────────
            if (selectedPeriod != ReportFilterPeriod.YEAR) {
                Text(
                    text = "Tap a day to see transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

// ─── Month grid ───────────────────────────────────────────────────────────────

@Composable
private fun MonthGrid(
    month: YearMonth,
    dailySpending: List<DailySpending>,
    isLoading: Boolean,
    primaryColor: Color,
    onDayClick: (LocalDate) -> Unit
) {
    val spendingMap = dailySpending.associateBy { it.date }
    val today = LocalDate.now()
    val firstDay = month.atDay(1)
    // Sunday-first offset: Sunday=0, Monday=1, …, Saturday=6
    val startOffset = firstDay.dayOfWeek.value % 7  // Sunday=7%7=0, Mon=1, …, Sat=6
    val daysInMonth = month.lengthOfMonth()
    // Previous month fill-in days
    val prevMonth = month.minusMonths(1)
    val prevMonthLen = prevMonth.lengthOfMonth()

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_HEADERS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Day cells — always 6 rows to keep card height stable
        val totalCells = 42  // 6 rows × 7 cols
        for (row in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1
                    when {
                        dayNumber < 1 -> {
                            // Previous month overflow
                            val prevDay = prevMonthLen + dayNumber
                            OtherMonthDayCell(dayNumber = prevDay, modifier = Modifier.weight(1f))
                        }
                        dayNumber > daysInMonth -> {
                            // Next month overflow
                            val nextDay = dayNumber - daysInMonth
                            OtherMonthDayCell(dayNumber = nextDay, modifier = Modifier.weight(1f))
                        }
                        else -> {
                            val date = month.atDay(dayNumber)
                            val spending = spendingMap[date]
                            DayCell(
                                dayNumber = dayNumber,
                                isToday = date == today,
                                spending = spending,
                                primaryColor = primaryColor,
                                isLoading = isLoading,
                                modifier = Modifier.weight(1f),
                                onClick = { onDayClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Week row ─────────────────────────────────────────────────────────────────

@Composable
private fun WeekRow(
    weekStart: LocalDate,
    dailySpending: List<DailySpending>,
    isLoading: Boolean,
    primaryColor: Color,
    onDayClick: (LocalDate) -> Unit
) {
    val spendingMap = dailySpending.associateBy { it.date }
    val today = LocalDate.now()
    // Align to Sunday-first: find the Sunday on or before weekStart
    val sundayStart = weekStart.minusDays(((weekStart.dayOfWeek.value % 7)).toLong())

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_HEADERS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 0 until 7) {
                val date = sundayStart.plusDays(i.toLong())
                DayCell(
                    dayNumber = date.dayOfMonth,
                    isToday = date == today,
                    spending = spendingMap[date],
                    primaryColor = primaryColor,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                    onClick = { onDayClick(date) }
                )
            }
        }
    }
}

// ─── Year grid ────────────────────────────────────────────────────────────────

@Composable
private fun YearGrid(
    year: Int,
    monthlySpending: List<MonthlySpending>,
    primaryColor: Color,
    onMonthTap: (YearMonth) -> Unit
) {
    val spendingMap = monthlySpending.associateBy { it.month }
    val maxAmount = monthlySpending.maxOfOrNull { it.amount } ?: 0.0
    val baseColor = MaterialTheme.colorScheme.surfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 3) {
                    val monthNum = row * 3 + col + 1
                    val ym = YearMonth.of(year, monthNum)
                    val spending = spendingMap[ym]
                    val fraction = if (maxAmount > 0 && spending != null) (spending.amount / maxAmount).toFloat() else 0f
                    val bgColor = lerp(baseColor, primaryColor.copy(alpha = 0.7f), fraction)
                    val monthName = ym.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val amountLabel = spending?.let { CurrencyFormatter.formatCompact(it.amount) } ?: ""
                    val cd = if (spending != null)
                        "$monthName, spending ${CurrencyFormatter.format(spending.amount)}"
                    else "$monthName, no spending"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { onMonthTap(ym) }
                            .semantics { contentDescription = cd },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (fraction > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            if (amountLabel.isNotEmpty()) {
                                Text(
                                    text = amountLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (fraction > 0.5f) Color.White.copy(alpha = 0.85f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Other-month faded cell ───────────────────────────────────────────────────

@Composable
private fun OtherMonthDayCell(dayNumber: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .height(56.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$dayNumber",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// ─── Day cell ─────────────────────────────────────────────────────────────────

@Composable
private fun DayCell(
    dayNumber: Int,
    isToday: Boolean,
    spending: DailySpending?,
    primaryColor: Color,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val hasSpending = spending != null && spending.amount > 0
    val amountLabel = if (hasSpending) CurrencyFormatter.formatCompact(spending!!.amount) else ""

    val cd = if (hasSpending)
        "$dayNumber, spending ${CurrencyFormatter.format(spending!!.amount)}"
    else "$dayNumber, no spending"

    Box(
        modifier = modifier
            .padding(2.dp)
            .height(56.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.TopCenter
    ) {
        if (!isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Circle around the day number
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isToday -> primaryColor
                                hasSpending -> primaryColor.copy(alpha = 0.15f)
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        fontWeight = if (isToday || hasSpending) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isToday -> Color.White
                            hasSpending -> primaryColor
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
                // Compact amount label below circle
                if (hasSpending) {
                    Text(
                        text = amountLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        } else {
            // Shimmer placeholder
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            )
        }
    }
}

// ─── Nav arrow button ─────────────────────────────────────────────────────────

@Composable
private fun NavArrowButton(
    direction: Int,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val label = if (direction < 0) "Previous period" else "Next period"
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(primaryColor.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (direction < 0)
                Icons.AutoMirrored.Filled.KeyboardArrowLeft
            else
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
