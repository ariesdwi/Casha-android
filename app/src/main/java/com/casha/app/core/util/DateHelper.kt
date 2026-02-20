package com.casha.app.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

/**
 * Utility for common date formatting and parsing operations.
 */
object DateHelper {

    private val isoFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Formats a [Date] to ISO 8601 string (e.g. "2026-02-20T10:30:00Z").
     */
    fun toISO8601(date: Date): String {
        val instant = date.toInstant()
        return isoFormatter.format(instant.atZone(ZoneId.of("UTC")))
    }

    /**
     * Parses an ISO 8601 string back to [Date].
     */
    fun fromISO8601(isoString: String): Date? {
        return try {
            val instant = Instant.parse(isoString)
            Date.from(instant)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats a [Date] for display (e.g. "Feb 20, 2026").
     */
    fun formatDisplay(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Formats a [Date] with time (e.g. "Feb 20, 2026 at 10:30 AM").
     */
    fun formatDisplayWithTime(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Returns a relative time description (e.g. "Today", "Yesterday", "3 days ago").
     */
    fun relativeDescription(date: Date): String {
        val now = LocalDate.now()
        val target = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val daysBetween = ChronoUnit.DAYS.between(target, now)

        return when {
            daysBetween == 0L -> "Today"
            daysBetween == 1L -> "Yesterday"
            daysBetween < 7L -> "$daysBetween days ago"
            daysBetween < 30L -> "${daysBetween / 7} weeks ago"
            else -> formatDisplay(date)
        }
    }

    /**
     * Formats to day name (e.g. "Monday", "Tuesday").
     */
    fun dayName(date: Date): String {
        val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Formats to short date (e.g. "20 Feb").
     */
    fun formatShort(date: Date): String {
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        return formatter.format(date)
    }
}
