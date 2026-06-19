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
            // 🎨 ကျွန်ုပ်တို့ ဖန်တီးခဲ့သော Custom Dark Theme အား စနစ်တကျ ချိတ်ဆက်ခြင်း
            AIIDETheme {
                // တစ်ပြင်လုံးအပြည့် Surface ဖြင့် ဖုံးအုပ်ပြီး Theme ၏ နောက်ခံအရောင်ကို ယူခြင်း
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🚀 File Explorer၊ Editor နှင့် Terminal ပါဝင်သော အဓိက AI-IDE Screen ကို စတင်မောင်းနှင်ခြင်း
                    MainIdeScreen()
                }
            }
        }
    }
}
