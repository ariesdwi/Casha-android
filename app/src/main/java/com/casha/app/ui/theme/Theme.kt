package com.casha.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CashaPrimaryLight,
    secondary = CashaAccentLight,
    background = CashaBackgroundLight,
    surface = CashaCardLight,
    onPrimary = CashaBackgroundLight,
    onSecondary = CashaBackgroundLight,
    onBackground = CashaTextPrimaryLight,
    onSurface = CashaTextPrimaryLight,
    onSurfaceVariant = CashaTextSecondaryLight,
    error = CashaDanger,
)

private val DarkColorScheme = darkColorScheme(
    primary = CashaPrimaryDark,
    secondary = CashaAccentDark,
    background = CashaBackgroundDark,
    surface = CashaCardDark,
    onPrimary = CashaBackgroundDark,
    onSecondary = CashaBackgroundDark,
    onBackground = CashaTextPrimaryDark,
    onSurface = CashaTextPrimaryDark,
    onSurfaceVariant = CashaTextSecondaryDark,
    error = CashaDanger,
)

@Composable
fun CashaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CashaTypography,
        shapes = CashaShapes,
        content = content
    )
}
