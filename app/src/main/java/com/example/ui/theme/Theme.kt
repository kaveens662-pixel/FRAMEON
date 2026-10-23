package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FrameonDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = ElectricSilver,
    secondary = ElectricSilver,
    onSecondary = ObsidianDeep,
    secondaryContainer = GraphiteSurface,
    onSecondaryContainer = ElectricSilver,
    tertiary = CyanAccent,
    onTertiary = ObsidianDeep,
    background = ObsidianDeep,
    onBackground = ElectricSilver,
    surface = ObsidianDark,
    onSurface = ElectricSilver,
    surfaceVariant = GraphiteCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderSubtle,
    outlineVariant = GlassFillMedium,
    error = DangerRuby,
    onError = Color.White
)

@Composable
fun FrameonTheme(
    content: @Composable () -> Unit
) {
    // FRAMEON enforces a dark, cinema studio aesthetic with translucent frosted glass
    MaterialTheme(
        colorScheme = FrameonDarkColorScheme,
        typography = Typography,
        content = content
    )
}
