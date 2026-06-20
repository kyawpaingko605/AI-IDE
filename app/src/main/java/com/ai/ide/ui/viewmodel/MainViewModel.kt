package com.ai.ide.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.ide.utils.AppCompiler
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainIdeScreen(
    compiler: AppCompiler,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // စမ်းသပ်ရန် ပရောဂျက် Folder လမ်းကြောင်း (သင့်ဖုန်းထဲက လမ်းကြောင်းအတိုင်း ပြောင်းလဲနိုင်ပါသည်)
    val projectDir = remember { File(context.filesDir, "SampleProject") }
    
    var isBuilding by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI - IDE", fontSize = 20.sp) },
                actions = {
                    // 🚀 ဒါကတော့ ဖုန်းထဲတင် APK Build လုပ်ပေးမယ့် ပင်မ Button ဖြစ်ပါတယ်
                    Button(
                        onClick = {
                            isBuilding = true
                            compiler.compileAndBuildApk(
                                projectDir = projectDir,
                                onSuccess = { apkFile ->
                                    isBuilding = false
                                    Toast.makeText(context, "APK Built Successfully: ${apkFile.name}", Toast.LENGTH_LONG).show()
                                },
                                onFailure = { error ->
                                    isBuilding = false
                                    Toast.makeText(context, "Build Failed: $error", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isBuilding,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(if (isBuilding) "Building..." else "Build APK", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ၁။ ဘယ်ဘက်ခြမ်း - ဖိုင်များကို လိုက်ကြည့်ရန် File Explorer View
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                FileExplorerView(
                    projectDir = projectDir,
                    onFileSelected = { file -> selectedFile = file }
                )
            }

            // ကြားခံမျဉ်းကြောင်း
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Gray)

            // ၂။ ညာဘက်ခြမ်း - ကုဒ်ရေးမည့် နေရာ (လောလောဆယ် စာသားအဖြစ်သာ ပြထားပါသည်)
            Box(
                modifier = Modifier.weight(2f).fillMaxSize().padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedFile != null) {
                    Text(text = "Editing: ${selectedFile?.name}\n\n(Code Editor coming soon...)", color = Color.LightGray)
                } else {
                    Text(text = "Select a file from explorer to edit", color = Color.Gray)
                }
            }
        }
    }
}
