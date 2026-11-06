package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoiceGangDarkColorScheme = darkColorScheme(
    primary = VoiceGangPrimary,
    onPrimary = VoiceGangOnPrimary,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = VoiceGangSecondary,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = VoiceGangTertiary,
    onTertiary = Color(0xFFFFFFFF),
    background = VoiceGangDarkBg,
    onBackground = VoiceGangOnSurface,
    surface = VoiceGangSurface,
    onSurface = VoiceGangOnSurface,
    surfaceVariant = VoiceGangSurfaceVariant,
    onSurfaceVariant = VoiceGangOnSurfaceVariant,
    outline = VoiceGangOutline
)

private val VoiceGangLightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = Color(0xFF0891B2),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = VoiceGangTertiary,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun VoiceGangTheme(
    darkTheme: Boolean = true, // Default to Voice Gang's signature dark aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) VoiceGangDarkColorScheme else VoiceGangLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
