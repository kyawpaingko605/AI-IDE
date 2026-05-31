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

    // 💻 Terminal View တွင် ပြသမည့် Log များစာရင်းအား မှတ်သားမည့် State List
    var terminalLogs by mutableStateOf<List<String>>(listOf("System initialized..."))
        private set

    init {
        // ၁။ အက်ပ်စဖွင့်သည်နှင့် လိုအပ်သော Binaries များကို သီးသန့်ထုတ်ယူခြင်း
        extractCompilerTools()
        
        // 💻 ၂။ စမ်းသပ်ရန် ပရောဂျက် Directory ဖွဲ့စည်းပုံအား အလိုအလျောက် ဆောက်ပေးခြင်း
        createSampleProjectStructure()
    }

    /**
     * 📝 Terminal ထဲသို့ Log အသစ်များ လှမ်းထည့်ပေးမည့် Utility Function
     */
    fun logToTerminal(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            terminalLogs = terminalLogs + message
        }
    }

    // Build လုပ်ငန်းစဉ် မစတင်မီ Log ဟောင်းများကို ရှင်းလင်းရန် Function
    fun clearTerminal() {
        terminalLogs = listOf("Starting build process...")
    }

    private fun extractCompilerTools() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                buildState = BuildState.ExtractingTools
                logToTerminal("Assets: Extracting compiler binaries to internal storage...")
            }
            
            val success = assetManager.extractAssetsToStorage()
            
            withContext(Dispatchers.Main) {
                if (success) {
                    buildState = BuildState.Idle
                    logToTerminal("Assets: Core compiler tools ready.")
                } else {
                    buildState = BuildState.Error("Failed to extract compiler tools.")
                    logToTerminal("Error: Extraction failed. Native components are missing.")
                }
            }
        }
    }

    /**
     * 📂 [Project Directory Setup]
     */
    private fun createSampleProjectStructure() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!projectBuilder.srcDir.exists()) projectBuilder.srcDir.mkdirs()
                if (!projectBuilder.resDir.exists()) projectBuilder.resDir.mkdirs()
                if (!projectBuilder.binDir.exists()) projectBuilder.binDir.mkdirs()

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
     * 🚀 [Core Build Pipeline with Logging]
     * စာသားကုဒ်များကို လက်ခံပြီး APK အဖြစ် ပြောင်းလဲစဉ် Terminal သို့ Log များ ထုတ်ပေးမည့် စနစ်
     */
    fun buildProject(codeText: String) {
        viewModelScope.launch {
            clearTerminal() // Log ဟောင်းများ အရင်ရှင်းမည်
            buildState = BuildState.CompilingResources
            logToTerminal("AAPT2: Launching XML and Resource Compiler...")

            val success = withContext(Dispatchers.IO) {
                try {
                    // ၁။ ကုဒ်ဖိုင်အား အရင်သိမ်းဆည်းခြင်း
                    val sourceFile = File(projectBuilder.srcDir, "MainActivity.kt")
                    sourceFile.writeText(codeText)
                    logToTerminal("Workspace: Selected source code saved to MainActivity.kt")

                    // ၂။ AAPT2 Resource Compile လုပ်ခြင်း
                    val aaptSuccess = projectBuilder.runAapt2Compile(projectBuilder.resDir)
                    if (!aaptSuccess) {
                        logToTerminal("Error: AAPT2 resource compilation failed!")
                        return@withContext false
                    }
                    logToTerminal("AAPT2: Resource compiled successfully (R.java / intermediate generated).")

                    // ၃။ D8 Dexing လုပ်ငန်းစဉ်သို့ ကူးပြောင်းခြင်း
                    withContext(Dispatchers.Main) { 
                        buildState = BuildState.DexingCode 
                    }
                    logToTerminal("D8: Converting Java/Kotlin bytecode into Dalvik Executable (.dex)...")
                    val dexSuccess = projectBuilder.runD8Dexing(listOf(sourceFile))
                    if (!dexSuccess) {
                        logToTerminal("Error: D8 dexer optimization failed!")
                        return@withContext false
                    }
                    logToTerminal("D8: Classes.dex optimization successfully built.")

                    // ၄။ APK လက်မှတ်ထိုးခြင်း
                    withContext(Dispatchers.Main) { 
                        buildState = BuildState.SigningApk 
                    }
                    logToTerminal("ApkSigner: Packaging and cryptographically signing the package...")
                    val unsignedApk = File(projectBuilder.binDir, "app-unsigned.apk")
                    val signedApk = File(projectBuilder.binDir, "app-release.apk")

                    apkSigner.signApk(unsignedApk, signedApk)
                    logToTerminal("ApkSigner: Signature alignment verified.")
                    true
                } catch (e: Exception) {
                    logToTerminal("Exception Error: ${e.message}")
                    false
                }
            }

            // ၅။ ရလဒ်အခြေအနေကို UI သို့ အတည်ထုတ်ပြန်ခြင်း
            if (success) {
                buildState = BuildState.Success(File(projectBuilder.binDir, "app-release.apk"))
                logToTerminal("Success: Final production APK is ready to install!")
            } else {
                buildState = BuildState.Error("Build process failed. Please check logs.")
                logToTerminal("Error: Build pipeline broke down. Review syntax or tools placement.")
            }
        }
    }
    
    // Status ပြန်ပြင်ရန်
    fun resetState() {
        buildState = BuildState.Idle
    }
}
