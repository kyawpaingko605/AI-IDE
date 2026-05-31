package com.ai.ide.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ai.ide.ui.viewmodel.BuildState
import com.ai.ide.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun MainIdeScreen() {
    val context = LocalContext.current
    // ViewModel ကို အသုံးပြု၍ State ၊ Build Pipeline နှင့် Terminal Logs များကို ထိန်းချုပ်ခြင်း
    val viewModel = remember { MainViewModel(context) }
    val buildState = viewModel.buildState
    
    // လက်ရှိ ဖွင့်လှစ်ပြီး ပြင်ဆင်နေသည့် ဖိုင်နှင့် ကုဒ်စာသားများကို မှတ်သားမည့် State
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var currentCode by remember { mutableStateOf("// Write your Kotlin code here\nfun main() {\n    println(\"Hello AI-IDE\")\n}") }

    // Build လုပ်ငန်းစဉ် အခြေအနေပြောင်းလဲမှုများကို စောင့်ကြည့်တုံ့ပြန်ခြင်း Logic
    LaunchedEffect(buildState) {
        when (buildState) {
            is BuildState.Success -> {
                Toast.makeText(context, "APK Built Successfully: ${buildState.apkFile.name}", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is BuildState.Error -> {
                Toast.makeText(context, buildState.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            // Compiler အလုပ်လုပ်နေချိန် မဟုတ်မှသာ Run ခလုတ်အား နှိပ်ခွင့်ပြုမည်
            if (buildState is BuildState.Idle) {
                FloatingActionButton(
                    onClick = { viewModel.buildProject(currentCode) }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run Build")
                }
            }
        }
    ) { innerPadding ->
        // 🛠️ တစ်ပြင်လုံးအား အပေါ်အောက် (Vertical Column) ပုံစံဖြင့် ဖွဲ့စည်းပြီး အောက်ခြေတွင် Terminal ထားရှိခြင်း
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1E1E1E))
        ) {
            // အပေါ်ပိုင်းအခြမ်း - File Explorer နှင့် Code Editor အား ဘေးချင်းယှဉ်ပြသမည့် နေရာ (Horizontal Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // ကျန်ရှိသော မျက်နှာပြင်နေရာလွတ်အကုန် ယူမည်
            ) {
                // ၁။ ဘယ်ဘက်ခြမ်း - ပရောဂျက်အတွင်းရှိ ဖိုင်တွဲများကို ပြသပေးမည့် File Explorer View
                FileExplorerView(
                    projectDir = context.filesDir, 
                    onFileSelected = { file ->
                        selectedFile = file
                        currentCode = file.readText() // ဖိုင်တစ်ခုအား ကလစ်နှိပ်ပါက ၎င်းထဲမှ ကုဒ်များအား Editor သို့ ပို့မည်
                    },
                    modifier = Modifier.weight(0.3f) // Layout တစ်ခုလုံး၏ ၃၀ ရာခိုင်နှုန်းအား ယူမည်
                )

                // Layout နှစ်ခုကြား ခွဲခြားပေးသည့် ဗဟိုဒေါင်လိုက်မျဉ်းကြောင်း (Vertical Divider)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color(0xFF3C3C3C))
                )

                // ၂။ ညာဘက်ခြမ်း - ကုဒ်များ ရေးသားတည်းဖြတ်မည့် Advanced Code Editor နေရာ
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.7f) // Layout တစ်ခုလုံး၏ ၇၀ ရာခိုင်နှုန်းအား ယူမည်
                ) {
                    AdvancedCodeEditor(
                        initialCode = currentCode,
                        onCodeChange = { updatedCode -> 
                            currentCode = updatedCode
                            // ကုဒ်များ ပြင်ဆင်လိုက်တိုင်း ရွေးချယ်ထားသော ဖိုင်ထဲသို့ အလိုအလျောက် Real-time သိမ်းဆည်းမည်
                            selectedFile?.writeText(updatedCode)
                        }
                    )

                    // Background တွင် Compiler Tools များ အလုပ်လုပ်နေစဉ် ပြသမည့် Loading Blur Screen Layer
                    if (buildState !is BuildState.Idle) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // အပေါ်ပိုင်းနှင့် အောက်ခြေ Terminal ကြား ခွဲခြားပေးသည့် အလျားလိုက်မျဉ်းကြောင်း (Horizontal Divider)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF3C3C3C))
            )

            // ၃။ အောက်ခြေအခြမ်း - Real-time Terminal Log Output View အား ပေါင်းစပ်ခြင်း
            TerminalView(
                logs = viewModel.terminalLogs,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
