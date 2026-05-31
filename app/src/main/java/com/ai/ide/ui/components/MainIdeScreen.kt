package com.ai.ide.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ai.ide.utils.ApkSigner
import com.ai.ide.utils.ProjectBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MainIdeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Core Engines များကို ယူနီဖောင်းဖြစ်အောင် ကြေညာခြင်း
    val projectBuilder = remember { ProjectBuilder(context) }
    val apkSigner = remember { ApkSigner(context) }
    
    // ရေးမှတ်ထားမည့် ကုဒ်များကို သိမ်းဆည်းရန် State
    var currentCode by remember { mutableStateOf("// Write your Kotlin code here\nfun main() {\n    println(\"Hello AI-IDE\")\n}") }

    Scaffold(
        floatingActionButton = {
            // 🛠️ APK ဆောက်လုပ်ပေးမည့် အဓိက "Build" ခလုတ်
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        Toast.makeText(context, "Building APK...", Toast.LENGTH_SHORT).show()
                        
                        val success = withContext(Dispatchers.IO) {
                            // ၁။ ကုဒ်ဖိုင်ကို အရင်ဆုံး သိမ်းဆည်းခြင်း
                            val sourceFile = File(projectBuilder.srcDir, "MainActivity.kt")
                            sourceFile.writeText(currentCode)
                            
                            // ၂။ AAPT2 ဖြင့် ရင်းမြစ်များကို Compile လုပ်ခြင်း
                            val aaptSuccess = projectBuilder.runAapt2Compile(projectBuilder.resDir)
                            if (!aaptSuccess) return@withContext false
                            
                            // ၃။ D8 ဖြင့် Dexer ပြောင်းလဲခြင်း (အစမ်းနမူနာအဖြစ် sourceFile အား တိုက်ရိုက်သွားခြင်း)
                            val dexSuccess = projectBuilder.runD8Dexing(listOf(sourceFile))
                            if (!dexSuccess) return@withContext false
                            
                            // ၄။ ထွက်လာသည့် Unsigned APK အား ဒစ်ဂျစ်တယ်လက်မှတ်ထိုးခြင်း
                            val unsignedApk = File(projectBuilder.binDir, "app-unsigned.apk")
                            val signedApk = File(projectBuilder.binDir, "app-release.apk")
                            
                            apkSigner.signApk(unsignedApk, signedApk)
                        }

                        if (success) {
                            Toast.makeText(context, "APK Built Successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Build Failed! Check your code.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run Build")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ရှေ့တွင် ဆောက်ခဲ့သည့် အဆင့်မြင့် Code Editor UI အား လှမ်းခေါ်ခြင်း
            AdvancedCodeEditor(
                initialCode = currentCode,
                onCodeChange = { updatedCode ->
                    currentCode = updatedCode
                }
            )
        }
    }
}
