package com.casha.app.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.casha.app.core.util.CurrencyFormatter

/**
 * [VisualTransformation] that formats a numeric string (digits + optional dot)
 * with thousand grouping separators on the integer part only.
 * E.g. "1000000.50" → "1,000,000.50". Up to 2 decimal digits are preserved.
 */
class CurrencyVisualTransformation(
    currencyCode: String = CurrencyFormatter.defaultCurrency
) : VisualTransformation {

    private val groupingSeparator: Char = ','

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        // Split into integer and decimal parts
        val dotIndex = original.indexOf('.')
        val intPart = if (dotIndex >= 0) original.substring(0, dotIndex) else original
        val decPart = if (dotIndex >= 0) original.substring(dotIndex) else "" // includes the dot

        val n = intPart.length
        if (n <= 3) {
            // No grouping needed for integer part, just concatenate
            val formatted = intPart + decPart
            if (formatted == original) return TransformedText(text, OffsetMapping.Identity)
            return TransformedText(AnnotatedString(formatted), OffsetMapping.Identity)
        }

        val firstGroupSize = if (n % 3 == 0) 3 else n % 3
        val totalSeparators = (n - 1) / 3 // number of commas inserted

        val formattedInt = buildString(n + totalSeparators) {
            for (i in intPart.indices) {
                if (i > 0 && (n - i) % 3 == 0) append(groupingSeparator)
                append(intPart[i])
            }
        }
        val formatted = formattedInt + decPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, original.length)
                if (o <= n) {
                    // Within integer part
                    val oi = o.coerceAtMost(n)
                    if (oi <= firstGroupSize) return oi
                    val seps = (oi - firstGroupSize - 1) / 3 + 1
                    return oi + seps
                }
                // Within decimal part: offset relative to end of integer
                val intTransformed = n + totalSeparators
                return intTransformed + (o - n)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, formatted.length)
                val intTransformed = formattedInt.length
                if (t <= intTransformed) {
                    var digits = 0
                    for (i in 0 until t) {
                        if (formattedInt[i] != groupingSeparator) digits++
                    }
                    return digits
                }
                // In decimal part
                return n + (t - intTransformed)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * [OutlinedTextField] that accepts digits only and displays them with live currency
 * thousand-grouping separators (e.g. "1.000.000" for IDR, "1,000,000" for USD).
 *
 * @param value Raw digits string (e.g. "1000000").
 * @param onValueChange Called with the filtered digits-only string.
 * @param currencyCode ISO 4217 currency code used to determine the grouping separator.
 */
@Composable
fun CurrencyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currencyCode: String = CurrencyFormatter.defaultCurrency,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    val visualTransformation = remember(currencyCode) {
        CurrencyVisualTransformation(currencyCode)
    }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Allow digits and at most one dot, max 2 decimal places
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val dotIdx = filtered.indexOf('.')
            val cleaned = if (dotIdx >= 0) {
                val intPart = filtered.substring(0, dotIdx).filter { it.isDigit() }
                val decPart = filtered.substring(dotIdx + 1).filter { it.isDigit() }.take(2)
                "$intPart.$decPart"
            } else {
                filtered.filter { it.isDigit() }
            }
            onValueChange(cleaned)
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        isError = isError,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
