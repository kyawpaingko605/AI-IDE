package com.ai.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ai.ide.ui.components.MainIdeScreen
import com.ai.ide.ui.theme.AIIDETheme // 💡 သင့်အက်ပ်တွင် သတ်မှတ်ထားသည့် Theme အမည်ကို အသုံးပြုပါ

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIIDETheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🚀 ကျွန်ုပ်တို့ ဆောက်ခဲ့သည့် အဓိက IDE Screen အား စတင်ခေါ်ယူခြင်း
                    MainIdeScreen()
                }
            }
        }
    }
}
