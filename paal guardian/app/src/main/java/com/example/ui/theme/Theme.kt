package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MilkGreenLight,
    onPrimary = Color.Black,
    primaryContainer = MilkGreenDark,
    onPrimaryContainer = MilkGreenContainer,
    secondary = MilkBlueAccent,
    onSecondary = Color.White,
    background = Color(0xFF121513),
    surface = Color(0xFF1A201C),
    onBackground = Color(0xFFE2E7E3),
    onSurface = Color(0xFFE2E7E3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = MilkGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = MilkGreenContainer,
    onPrimaryContainer = MilkGreenOnContainer,
    secondary = MilkBlueAccent,
    onSecondary = Color.White,
    secondaryContainer = MilkBlueContainer,
    onSecondaryContainer = MilkBlueOnContainer,
    background = AppBackground,
    surface = AppSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEFF3EE),
    onSurfaceVariant = TextSecondary,
    outline = AppCardStroke,
    error = TempWarning,
    onError = Color.White,
    errorContainer = TempWarningBg,
    onErrorContainer = TempWarning
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
