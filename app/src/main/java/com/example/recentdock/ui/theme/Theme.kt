package com.example.recentdock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B63D3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E4FF),
    onPrimaryContainer = Color(0xFF171B61),
    secondary = Color(0xFF5D5F73),
    secondaryContainer = Color(0xFFE2E1F3),
    background = Color(0xFFFAFAFC),
    surface = Color(0xFFFAFAFC),
    surfaceVariant = Color(0xFFE7E7EC),
    onSurface = Color(0xFF1B1B1F),
    onSurfaceVariant = Color(0xFF46464F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBEC2FF),
    onPrimary = Color(0xFF252B8F),
    primaryContainer = Color(0xFF3D439F),
    onPrimaryContainer = Color(0xFFE2E4FF),
    secondary = Color(0xFFC6C5D9),
    secondaryContainer = Color(0xFF454657),
    background = Color(0xFF111114),
    surface = Color(0xFF111114),
    surfaceVariant = Color(0xFF46464F),
    onSurface = Color(0xFFE5E1E6),
    onSurfaceVariant = Color(0xFFC7C5D0),
)

@Composable
fun RecentDockTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
