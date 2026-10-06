package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0F1220),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFD6DCFF),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF4A0A1A),
    secondaryContainer = Color(0xFF6B1D30),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = AnimeTertiary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = AnimePrimary,
    onPrimary = Color.White,
    primaryContainer = AnimePrimaryContainer,
    onPrimaryContainer = AnimeOnPrimaryContainer,
    secondary = AnimeSecondary,
    onSecondary = Color.White,
    secondaryContainer = AnimeSecondaryContainer,
    onSecondaryContainer = AnimeOnSecondaryContainer,
    tertiary = AnimeTertiary,
    background = AnimeBackground,
    onBackground = AnimeTextPrimary,
    surface = AnimeSurface,
    onSurface = AnimeTextPrimary,
    surfaceVariant = AnimeSurfaceVariant,
    onSurfaceVariant = AnimeTextSecondary
)

@Composable
fun ZeroPlayerTheme(
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
