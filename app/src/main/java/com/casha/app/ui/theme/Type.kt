package com.casha.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CashaTypography = Typography(
    // Large title — used for main balance display
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // Headline — section titles
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Subheadline — descriptions, subtitles
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // Body — standard text
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Caption — timestamps, small labels
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Button text
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

// Custom text styles for specific use cases
object CashaTextStyles {
    val balanceAmount = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 44.sp
    )
    val statAmount = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 20.sp
    )
}
