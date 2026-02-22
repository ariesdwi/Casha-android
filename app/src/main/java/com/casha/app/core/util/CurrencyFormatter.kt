package com.casha.app.core.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility for formatting monetary amounts with proper currency symbols.
 * Ported from iOS CurrencyFormatter.swift — supports 40+ currencies with
 * locale-aware formatting, custom symbols, and correct fraction digits.
 */
object CurrencyFormatter {

    /** Default currency code. Can be overridden at app startup from DataStore. */
    var defaultCurrency: String = "IDR"

    // ── Currency Config ──

    private data class CurrencyConfig(
        val locale: Locale,
        val symbol: String,
        val maxFractionDigits: Int
    )

    private val currencyConfigs: Map<String, CurrencyConfig> = mapOf(
        // ASEAN
        "IDR" to CurrencyConfig(Locale("id", "ID"), "Rp ", 0),
        "SGD" to CurrencyConfig(Locale("en", "SG"), "S$", 2),
        "MYR" to CurrencyConfig(Locale("ms", "MY"), "RM", 2),
        "VND" to CurrencyConfig(Locale("vi", "VN"), "₫", 0),
        "THB" to CurrencyConfig(Locale("th", "TH"), "฿", 2),
        "PHP" to CurrencyConfig(Locale("en", "PH"), "₱", 2),
        "BND" to CurrencyConfig(Locale("ms", "BN"), "B$", 2),
        "KHR" to CurrencyConfig(Locale("km", "KH"), "៛", 0),
        "LAK" to CurrencyConfig(Locale("lo", "LA"), "₭", 0),
        "MMK" to CurrencyConfig(Locale("my", "MM"), "Ks", 0),
        "USD" to CurrencyConfig(Locale.US, "$", 2),

        // Other Asian
        "CNY" to CurrencyConfig(Locale.CHINA, "¥", 2),
        "JPY" to CurrencyConfig(Locale.JAPAN, "¥", 0),
        "KRW" to CurrencyConfig(Locale.KOREA, "₩", 0),
        "INR" to CurrencyConfig(Locale("en", "IN"), "₹", 2),
        "BDT" to CurrencyConfig(Locale("bn", "BD"), "৳", 2),
        "PKR" to CurrencyConfig(Locale("ur", "PK"), "₨", 2),
        "LKR" to CurrencyConfig(Locale("si", "LK"), "Rs", 2),
        "NPR" to CurrencyConfig(Locale("ne", "NP"), "₨", 2),

        // Middle East
        "SAR" to CurrencyConfig(Locale("ar", "SA"), "﷼", 2),
        "AED" to CurrencyConfig(Locale("ar", "AE"), "د.إ", 2),
        "QAR" to CurrencyConfig(Locale("ar", "QA"), "﷼", 2),
        "KWD" to CurrencyConfig(Locale("ar", "KW"), "د.ك", 3),
        "OMR" to CurrencyConfig(Locale("ar", "OM"), "﷼", 3),
        "BHD" to CurrencyConfig(Locale("ar", "BH"), ".د.ب", 3),

        // Americas
        "CAD" to CurrencyConfig(Locale("en", "CA"), "C$", 2),
        "BRL" to CurrencyConfig(Locale("pt", "BR"), "R$", 2),
        "MXN" to CurrencyConfig(Locale("es", "MX"), "$", 2),
        "ARS" to CurrencyConfig(Locale("es", "AR"), "$", 2),

        // Europe
        "EUR" to CurrencyConfig(Locale.GERMANY, "€", 2),
        "GBP" to CurrencyConfig(Locale.UK, "£", 2),
        "CHF" to CurrencyConfig(Locale("de", "CH"), "CHF", 2),
        "SEK" to CurrencyConfig(Locale("sv", "SE"), "kr", 2),
        "NOK" to CurrencyConfig(Locale("nb", "NO"), "kr", 2),
        "DKK" to CurrencyConfig(Locale("da", "DK"), "kr", 2),
        "RUB" to CurrencyConfig(Locale("ru", "RU"), "₽", 2),
        "TRY" to CurrencyConfig(Locale("tr", "TR"), "₺", 2),

        // Oceania
        "AUD" to CurrencyConfig(Locale("en", "AU"), "A$", 2),
        "NZD" to CurrencyConfig(Locale("en", "NZ"), "NZ$", 2),

        // Africa
        "ZAR" to CurrencyConfig(Locale("en", "ZA"), "R", 2),
        "EGP" to CurrencyConfig(Locale("ar", "EG"), "£", 2),
        "NGN" to CurrencyConfig(Locale("en", "NG"), "₦", 2),
        "KES" to CurrencyConfig(Locale("en", "KE"), "KSh", 2),
        "GHS" to CurrencyConfig(Locale("en", "GH"), "₵", 2)
    )

    // ── Public API ──

    /**
     * Formats [amount] with the given [currencyCode] (ISO 4217).
     * Falls back to [defaultCurrency] if no code is provided.
     */
    fun format(amount: Double, currencyCode: String? = null): String {
        val code = (currencyCode ?: defaultCurrency).uppercase()
        val config = currencyConfigs[code]

        return if (config != null) {
            val formatter = NumberFormat.getCurrencyInstance(config.locale)
            formatter.maximumFractionDigits = config.maxFractionDigits
            formatter.minimumFractionDigits = config.maxFractionDigits
            try {
                formatter.currency = Currency.getInstance(code)
            } catch (_: Exception) { /* fallback to locale default */ }
            formatter.format(amount)
        } else {
            // Unknown currency — generic formatting
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            formatter.maximumFractionDigits = 2
            try {
                formatter.currency = Currency.getInstance(code)
            } catch (_: Exception) { /* ignore */ }
            formatter.format(amount)
        }
    }

    /**
     * Formats a raw input string (digits only) as currency.
     */
    fun formatInput(input: String, currencyCode: String? = null): String {
        val digitsOnly = input.replace(Regex("[^0-9]"), "")
        val amount = digitsOnly.toDoubleOrNull() ?: 0.0
        return format(amount, currencyCode)
    }

    /**
     * Extracts the raw numeric value from a formatted currency string.
     */
    fun extractRawValue(formatted: String): Double {
        val digitsOnly = formatted.replace(Regex("[^0-9]"), "")
        return digitsOnly.toDoubleOrNull() ?: 0.0
    }

    /**
     * Returns the currency symbol for the given [currencyCode].
     */
    fun symbol(currencyCode: String? = null): String {
        val code = (currencyCode ?: defaultCurrency).uppercase()
        return currencyConfigs[code]?.symbol ?: code
    }

    /**
     * Formats [amount] as a compact string (e.g. "1.2K", "3.5M").
     */
    fun formatCompact(amount: Double, currencyCode: String? = null): String {
        val sym = symbol(currencyCode)
        return when {
            amount >= 1_000_000_000 -> "$sym${String.format("%.1fB", amount / 1_000_000_000)}"
            amount >= 1_000_000 -> "$sym${String.format("%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "$sym${String.format("%.1fK", amount / 1_000)}"
            else -> "$sym${String.format("%.0f", amount)}"
        }
    }
}
