package com.ai.ide.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// 💻 AI-IDE အတွက် သီးသန့်သတ်မှတ်ထားသော Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = EditorBg,          // ကျွန်ုပ်တို့ Color.kt တွင် သတ်မှတ်ခဲ့သော EditorBg အား သုံးထားပါသည်
    surface = LineNumberBar
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun AIIDETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // IDE စနစ်ဖြစ်သောကြောင့် အမြဲတမ်း Dark Theme အား ဦးစားပေးအသုံးပြုမည်
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status Bar နှင့် Navigation Bar အား IDE Theme အရောင်များအတိုင်း ညှိပေးခြင်း
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // 💡 ကျွန်ုပ်တို့ သတ်မှတ်ခဲ့သော Typography စနစ်အား ချိတ်ဆက်ခြင်း
        shapes = Shapes,         // 💡 ကျွန်ုပ်တို့ သတ်မှတ်ခဲ့သော Shapes စနစ်အား ချိတ်ဆက်ခြင်း
        content = content
    )
}
