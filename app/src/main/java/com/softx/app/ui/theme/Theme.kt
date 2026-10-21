package com.softx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

private val PocketLightColors = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentPurple,
    background = SurfaceBase,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun PocketProNativeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PocketLightColors,
        typography = PocketTypography,
        content = content,
    )
}
