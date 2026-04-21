package com.prodash.reminders.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ObsidianDark = darkColorScheme(
    primary = Color(0xFFE6E6E6),
    onPrimary = Color(0xFF050505),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color(0xFFEAEAEA),
    tertiary = Color(0xFFBDBDBD),
    surface = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF171717),
    onSurface = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFFAFAFAF),
    outline = Color(0xFF626262),
    outlineVariant = Color(0xFF2A2A2A),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ObsidianDark,
        typography = Typography,
        content = content,
    )
}
