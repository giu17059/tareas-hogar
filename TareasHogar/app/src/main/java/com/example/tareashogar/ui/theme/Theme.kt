package com.example.tareashogar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D6B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F0DC),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFFE07B39),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2E1500),
    background = Color(0xFFF7FAF8),
    surface = Color(0xFFF7FAF8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DD5BF),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF005142),
    onPrimaryContainer = Color(0xFFB8F0DC),
    secondary = Color(0xFFFFB783),
    onSecondary = Color(0xFF4B2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDCC2)
)

@Composable
fun TareasHogarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
