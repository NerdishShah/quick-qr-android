package com.quickqr.scanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = ScanAccent,
    onPrimary = Color(0xFF001018),
    secondary = ScanAccent,
    background = ScanBackground,
    onBackground = OnScan,
    surface = SurfaceDark,
    onSurface = OnScan,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = Color(0xFFB8C0CC)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF006D8F),
    onPrimary = Color.White,
    secondary = Color(0xFF4CC9F0),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF101418),
    surface = Color.White,
    onSurface = Color(0xFF101418)
)

@Composable
fun QuickQrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
