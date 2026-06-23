package com.casha.app.widget.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

/**
 * Enhanced currency formatter for widget displays.
 * Supports both short format (compact spaces) and full format (detailed views).
 * 
 * Features:
 * - Locale-aware formatting
 * - Multiple currency support
 * - Compact abbreviations (IDR: rb/jt/M, USD/etc: k/M/B)
 * - Proper number grouping
 * 
 * Examples:
 * - formatShort(250000, "IDR") → "Rp250rb"
 * - formatShort(3500000, "IDR") → "Rp3.5jt"
 * - formatFull(250000, "IDR") → "Rp250.000"
 * - formatShort(2500, "USD") → "$2.5k"
 */
object WidgetCurrencyFormatter {

    /**
     * Format amount in compact short form for limited widget space.
     * 
     * @param amount The monetary amount
     * @param currency Currency code (ISO 4217)
     * @param locale Locale for number formatting (default: system locale)
     * @return Compact formatted string
     */
    fun formatShort(
        amount: Double, 
        currency: String = "IDR",
        locale: Locale = Locale.getDefault()
    ): String {
        val symbol = currencySymbol(currency)
        val absAmount = abs(amount)

        return when (currency) {
            "IDR" -> when {
                absAmount >= 1_000_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000_000, locale)}M"
                absAmount >= 1_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000, locale)}jt"
                absAmount >= 1_000 -> "${symbol}${formatDecimal(absAmount / 1_000, locale)}rb"
                else -> "${symbol}${absAmount.toLong()}"
            }
            else -> when {
                absAmount >= 1_000_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000_000, locale)}B"
                absAmount >= 1_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000, locale)}M"
                absAmount >= 1_000 -> "${symbol}${formatDecimal(absAmount / 1_000, locale)}k"
                else -> "${symbol}${absAmount.toLong()}"
            }
        }
    }

    /**
     * Format amount with full number and thousands separators.
     * 
     * @param amount The monetary amount
     * @param currency Currency code (ISO 4217)
     * @param locale Locale for number formatting (default: system locale)
     * @return Fully formatted currency string
     */
    fun formatFull(
        amount: Double, 
        currency: String = "IDR",
        locale: Locale = Locale.getDefault()
    ): String {
        val symbol = currencySymbol(currency)
        val separator = if (currency == "IDR") '.' else ','
        val wholeAmount = abs(amount).toLong()
        val formatted = wholeAmount.toString().reversed().chunked(3).joinToString("$separator").reversed()
        return "$symbol$formatted"
    }
    
    /**
     * Format amount using system NumberFormat (most accurate for locale).
     * Use when widget has enough space for full currency display.
     * 
     * @param amount The monetary amount
     * @param currency Currency code (ISO 4217)
     * @param locale Locale for number formatting
     * @return Locale-formatted currency string
     */
    fun formatWithLocale(
        amount: Double,
        currency: String = "IDR",
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = Currency.getInstance(currency)
                maximumFractionDigits = 0 // No decimals for widget display
            }
            numberFormat.format(amount)
        } catch (e: Exception) {
            // Fallback to manual formatting if locale/currency not supported
            formatFull(amount, currency, locale)
        }
    }

    /**
     * Format a decimal number for short display.
     * Removes trailing zeros and decimal point if whole number.
     * 
     * @param value The numeric value
     * @param locale Locale for decimal formatting
     * @return Formatted decimal string
     */
    private fun formatDecimal(value: Double, locale: Locale): String {
        return if (value == value.toLong().toDouble()) {
            // Whole number, no decimals needed
            value.toLong().toString()
        } else {
            // Has decimal, show one decimal place
            val symbols = DecimalFormatSymbols(locale)
            val formatter = DecimalFormat("#.#", symbols)
            formatter.format(value).trimEnd('0').trimEnd(symbols.decimalSeparator)
        }
    }

    /**
     * Get the currency symbol for a given currency code.
     * 
     * @param currency Currency code (ISO 4217)
     * @return Currency symbol
     */
    private fun currencySymbol(currency: String): String = when (currency) {
        "IDR" -> "Rp"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "SGD" -> "S$"
        "MYR" -> "RM"
        "THB" -> "฿"
        "PHP" -> "₱"
        "VND" -> "₫"
        "AUD" -> "A$"
        "CAD" -> "C$"
        "KRW" -> "₩"
        "INR" -> "₹"
        "CNY" -> "¥"
        "HKD" -> "HK$"
        "CHF" -> "CHF"
        "SEK" -> "kr"
        "NOK" -> "kr"
        "DKK" -> "kr"
        else -> "$currency "
    }
    
    /**
     * Get human-readable abbreviation suffix for a currency.
     * 
     * @param currency Currency code
     * @return Map of amount ranges to abbreviations
     */
    fun getAbbreviations(currency: String): Map<String, String> = when (currency) {
        "IDR" -> mapOf(
            "thousand" to "rb",
            "million" to "jt",
            "billion" to "M"
        )
        else -> mapOf(
            "thousand" to "k",
            "million" to "M",
            "billion" to "B"
        )
    }
}
