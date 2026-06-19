package com.ai.ide.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// IDE အတွက် အမှောင် Theme (Dark Color Palette)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF007ACC),
    secondary = Color(0xFF252526),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF252526),
    onPrimary = Color.White,
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4)
)

// ပုံမှန်အလင်း Theme (Light Color Palette)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007ACC),
    secondary = Color(0xFFF3F3F3),
    background = Color.White,
    surface = Color(0xFFF3F3F3),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun AIIDETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status Bar နှင့် Navigation Bar ကို Transparent ပြုလုပ်ခြင်း
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
