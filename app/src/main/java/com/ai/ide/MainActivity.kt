package com.ai.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ai.ide.ui.components.MainIdeScreen
import com.ai.ide.ui.theme.AIIDETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 🎨 ကျွန်ုပ်တို့ သတ်မှတ်ခဲ့သည့် Custom Jetpack Compose Theme အား ချိတ်ဆက်ခြင်း
            AIIDETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🚀 အဓိက AI-IDE Screen မျက်နှာပြင်အား စတင်မောင်းနှင်ခြင်း
                    MainIdeScreen()
                }
            }
        }
    }
}
