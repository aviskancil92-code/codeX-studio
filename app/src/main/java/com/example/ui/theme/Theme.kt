package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CodeXDarkColorScheme = darkColorScheme(
    primary = CodeXPrimary,
    onPrimary = Color.Black,
    primaryContainer = CodeXAccent,
    onPrimaryContainer = Color.White,
    secondary = CodeXSecondary,
    onSecondary = Color.Black,
    tertiary = CodeXTertiary,
    background = CodeXDarkBg,
    onBackground = CodeXTextPrimary,
    surface = CodeXSidebarBg,
    onSurface = CodeXTextPrimary,
    surfaceVariant = CodeXPanelBg,
    onSurfaceVariant = CodeXTextSecondary,
    outline = CodeXBorder,
    outlineVariant = CodeXBorderSubtle,
    error = CodeXError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CodeXDarkColorScheme,
        typography = Typography,
        content = content
    )
}

