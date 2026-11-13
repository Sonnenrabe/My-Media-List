package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C3252),
    onPrimaryContainer = Color(0xFFE0E7FF),

    secondary = MediaSeriesCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C1D2A),
    onSecondaryContainer = Color(0xFFFFE4E6),

    tertiary = MediaMangaMint,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF133E31),
    onTertiaryContainer = Color(0xFFD1FAE5),

    background = GameDarkBackground,
    onBackground = TextPrimary,

    surface = GameDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = GameDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = NeonRed,
    onError = Color.White,

    outline = Color(0xFF384364),
    outlineVariant = Color(0xFF232A40)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
