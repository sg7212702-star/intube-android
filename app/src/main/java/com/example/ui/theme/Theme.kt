package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = InTubePink,
    onPrimary = Color.White,
    primaryContainer = InTubePinkDark,
    onPrimaryContainer = Color.White,
    secondary = InTubeGold,
    onSecondary = Color.Black,
    secondaryContainer = InTubeSurfaceVariant,
    onSecondaryContainer = InTubeGold,
    tertiary = InTubeCyan,
    onTertiary = Color.Black,
    background = InTubeDarkBg,
    onBackground = InTubeTextPrimary,
    surface = InTubeSurface,
    onSurface = InTubeTextPrimary,
    surfaceVariant = InTubeSurfaceVariant,
    onSurfaceVariant = InTubeTextSecondary,
    outline = InTubeCardBorder,
    outlineVariant = Color(0x1AFFFFFF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // InTube is intentionally a dark luxury entertainment app
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
