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
import com.ai.ide.utils.AppCompiler // Compiler System ကို သုံးနိုင်ရန် Import လုပ်ခြင်း

class MainActivity : ComponentActivity() {
    
    // အက်ပ်တစ်ခုလုံးမှာ Button နှိပ်ရင် လှမ်းမောင်းမည့် ပင်မ Compiler Engine ကို ကြေညာခြင်း
    private lateinit var appCompiler: AppCompiler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Compiler ကို လက်ရှိ Context ပေးပြီး အဆင်သင့် ဆောက်ထားခြင်း
        appCompiler = AppCompiler(this)
        
        setContent {
            // 🎨 ကျွန်ုပ်တို့ ဖန်တီးခဲ့သော Custom Dark Theme အား စနစ်တကျ ချိတ်ဆက်ခြင်း
            AIIDETheme {
                // တစ်ပြင်လုံးအပြည့် Surface ဖြင့် ဖုံးအုပ်ပြီး Theme ၏ နောက်ခံအရောင်ကို ယူခြင်း
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🚀 MainIdeScreen ထဲသို့ နောက်ကွယ်ကနေ APK Build လုပ်ပေးမယ့် Compiler Engine ကိုပါ ထည့်ပေးလိုက်ခြင်း
                    MainIdeScreen(compiler = appCompiler)
                }
            }
        }
    }
}
