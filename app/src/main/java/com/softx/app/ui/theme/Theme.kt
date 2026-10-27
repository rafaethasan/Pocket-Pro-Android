package com.softx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PocketDarkColors = darkColorScheme(
    primary = AccentPurple,
    secondary = AccentBlue,
    tertiary = AccentPink,
    background = SurfaceBase,
    surface = SurfaceCard,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun PocketProNativeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PocketDarkColors,
        typography = PocketTypography,
        content = content,
    )
}
