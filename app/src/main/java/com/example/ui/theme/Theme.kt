package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HudColorScheme = darkColorScheme(
    primary = HudCyanGlow,
    onPrimary = HudVoidDark,
    primaryContainer = HudSurfaceVariant,
    onPrimaryContainer = HudCyanGlow,
    secondary = HudBlueElectric,
    onSecondary = HudVoidDark,
    secondaryContainer = HudSurfaceVariant,
    onSecondaryContainer = HudTextPrimary,
    tertiary = HudAmberGold,
    onTertiary = HudVoidDark,
    background = HudVoidDark,
    onBackground = HudTextPrimary,
    surface = HudSurface,
    onSurface = HudTextPrimary,
    surfaceVariant = HudSurfaceVariant,
    onSurfaceVariant = HudTextSecondary,
    outline = HudCyanDim,
    error = HudAlertRed,
    onError = HudVoidDark
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HudColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JarvisTheme(content = content)
}
