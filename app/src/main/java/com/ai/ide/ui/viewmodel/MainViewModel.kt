package com.ai.ide.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.ide.utils.ApkSigner
import com.ai.ide.utils.AssetManager
import com.ai.ide.utils.ProjectBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Build လုပ်ဆောင်ချက်၏ အခြေအနေကို မှတ်သားရန် Sealed Class
sealed class BuildState {
    object Idle : BuildState()
    object ExtractingTools : BuildState()
    object CompilingResources : BuildState()
    object DexingCode : BuildState()
    object SigningApk : BuildState()
    data class Success(val apkFile: File) : BuildState()
    data class Error(val message: String) : BuildState()
}

class MainViewModel(context: Context) : ViewModel() {

    // Core Engines များကို Initialize လုပ်ခြင်း
    private val projectBuilder = ProjectBuilder(context)
    private val apkSigner = ApkSigner(context)
    private val assetManager = AssetManager(context)

    // UI မှ လှမ်းကြည့်မည့် Build Status State
    var buildState by mutableStateOf<BuildState>(BuildState.Idle)
        private set

    init {
        // ၁။ အက်ပ်စဖွင့်သည်နှင့် လိုအပ်သော Binaries များကို သီးသန့်ထုတ်ယူခြင်း
        extractCompilerTools()
        
        // 💻 ၂။ စမ်းသပ်ရန် ပရောဂျက် Directory ဖွဲ့စည်းပုံအား အလိုအလျောက် ဆောက်ပေးခြင်း
        createSampleProjectStructure()
    }

    private fun extractCompilerTools() {
        viewModelScope.launch(Dispatchers.IO) {
            // Main Thread ပေါ်တွင် State ပြောင်းလဲခြင်း
            withContext(Dispatchers.Main) {
                buildState = BuildState.ExtractingTools
            }
            
            val success = assetManager.extractAssetsToStorage()
            
            withContext(Dispatchers.Main) {
                buildState = if (success) BuildState.Idle else BuildState.Error("Failed to extract compiler tools.")
            }
        }
    }

    /**
     * 📂 [Project Directory Setup]
     * အက်ပ်ပထမဆုံးပွင့်ချိန်တွင် လိုအပ်သော src, res, bin ဖိုဒါများနှင့် MainActivity.kt ကို ဆောက်ပေးမည့် စနစ်
     */
    private fun createSampleProjectStructure() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // အဓိက ဖိုင်တွဲများ ရှိမရှိ စစ်ဆေးပြီး မရှိပါက ဆောက်ပေးခြင်း
                if (!projectBuilder.srcDir.exists()) projectBuilder.srcDir.mkdirs()
                if (!projectBuilder.resDir.exists()) projectBuilder.resDir.mkdirs()
                if (!projectBuilder.binDir.exists()) projectBuilder.binDir.mkdirs()

                // အခြေခံ Sample ကုဒ်ဖိုင်တစ်ခုအား အလိုအလျောက် ဖန်တီးပေးခြင်း
                val mainActivityFile = File(projectBuilder.srcDir, "MainActivity.kt")
                if (!mainActivityFile.exists()) {
                    mainActivityFile.writeText(
                        "package com.example.myapp\n\n" +
                        "fun main() {\n" +
                        "    println(\"Hello from AI-IDE Engine!\")\n" +
                        "}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 🚀 [Core Build Pipeline]
     * စာသားကုဒ်များကို လက်ခံပြီး APK အဖြစ် အဆင့်ဆင့် ပြောင်းလဲပေးသည့် အဓိက Function
     */
    fun buildProject(codeText: String) {
        viewModelScope.launch {
            // စတင်ချိန်တွင် Compiling Resources State သို့ ပြောင်းလဲခြင်း
            buildState = BuildState.CompilingResources

            val success = withContext(Dispatchers.IO) {
                try {
                    // ၁။ ကုဒ်ဖိုင်အား အရင်သိမ်းဆည်းခြင်း
                    val sourceFile = File(projectBuilder.srcDir, "MainActivity.kt")
                    sourceFile.writeText(codeText)

                    // ၂။ AAPT2 Resource Compile လုပ်ခြင်း
                    val aaptSuccess = projectBuilder.runAapt2Compile(projectBuilder.resDir)
                    if (!aaptSuccess) return@withContext false

                    // ၃။ D8 Dexing လုပ်ငန်းစဉ်သို့ ကူးပြောင်းခြင်း State ပြောင်းမည်
                    withContext(Dispatchers.Main) { 
                        buildState = BuildState.DexingCode 
                    }
                    val dexSuccess = projectBuilder.runD8Dexing(listOf(sourceFile))
                    if (!dexSuccess) return@withContext false

                    // ၄။ APK လက်မှတ်ထိုးခြင်း State ပြောင်းမည်
                    withContext(Dispatchers.Main) { 
                        buildState = BuildState.SigningApk 
                    }
                    val unsignedApk = File(projectBuilder.binDir, "app-unsigned.apk")
                    val signedApk = File(projectBuilder.binDir, "app-release.apk")

                    apkSigner.signApk(unsignedApk, signedApk)
                } catch (e: Exception) {
                    false
                }
            }

            // ၅။ ရလဒ်အခြေအနေကို UI သို့ အတည်ထုတ်ပြန်ခြင်း
            buildState = if (success) {
                BuildState.Success(File(projectBuilder.binDir, "app-release.apk"))
            } else {
                BuildState.Error("Build process failed. Please check logs.")
            }
        }
    }
    
    // Status ပြန်ပြင်ရန်
    fun resetState() {
        buildState = BuildState.Idle
    }
}
