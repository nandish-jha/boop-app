package com.prodash.reminders.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ObsidianDark = darkColorScheme(
    primary = Color(0xFFC0C1FF),
    onPrimary = Color(0xFF1000A9),
    primaryContainer = Color(0xFF8083FF),
    onPrimaryContainer = Color(0xFF0D0096),
    tertiary = Color(0xFFFFB783),
    surface = Color(0xFF111317),
    surfaceContainerLowest = Color(0xFF0C0E12),
    surfaceContainerLow = Color(0xFF1A1C20),
    surfaceContainer = Color(0xFF1E2024),
    surfaceContainerHigh = Color(0xFF282A2E),
    onSurface = Color(0xFFE2E2E8),
    onSurfaceVariant = Color(0xFFC7C4D7),
    outline = Color(0xFF908FA0),
    outlineVariant = Color(0xFF464554),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ObsidianDark,
        typography = Typography,
        content = content,
    )
}
