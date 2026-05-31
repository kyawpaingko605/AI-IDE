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

    val projectBuilder = ProjectBuilder(context)
    private val apkSigner = ApkSigner(context)
    private val assetManager = AssetManager(context)

    var buildState by mutableStateOf<BuildState>(BuildState.Idle)
        private set

    var terminalLogs by mutableStateOf<List<String>>(listOf("System ready."))
        private set

    init {
        extractCompilerTools()
        createSampleProjectStructure()
    }

    fun logToTerminal(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            terminalLogs = terminalLogs + message
        }
    }

    fun clearTerminal() {
        terminalLogs = listOf("Starting build process...")
    }

    private fun extractCompilerTools() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { buildState = BuildState.ExtractingTools }
            val success = assetManager.extractAssetsToStorage()
            withContext(Dispatchers.Main) {
                buildState = BuildState.Idle
                if (success) logToTerminal("System: Compiler binaries are synchronized.")
                else logToTerminal("Error: Native components missing.")
            }
        }
    }

    private fun createSampleProjectStructure() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val layoutDir = File(projectBuilder.resDir, "layout").apply { if (!exists()) mkdirs() }
                val valuesDir = File(projectBuilder.resDir, "values").apply { if (!exists()) mkdirs() }
                
                val manifestFile = File(projectBuilder.projectDir, "AndroidManifest.xml")
                if (!manifestFile.exists()) {
                    manifestFile.writeText("""
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example.myapp">
                            <application android:label="My Demo App">
                                <activity android:name=".MainActivity" android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>
                            </application>
                        </manifest>
                    """.trimIndent())
                }

                val stringsFile = File(valuesDir, "strings.xml")
                if (!stringsFile.exists()) {
                    stringsFile.writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <string name=\"app_name\">My Demo App</string>\n</resources>")
                }

                val mainFile = File(projectBuilder.srcDir, "MainActivity.kt")
                if (!mainFile.exists()) {
                    mainFile.writeText("package com.example.myapp\n\nfun main() {\n    println(\"Hello Android Studio Monospace!\")\n}")
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun buildProject(codeText: String) {
        viewModelScope.launch {
            clearTerminal()
            buildState = BuildState.CompilingResources
            logToTerminal("AAPT2: Compiling assets and XML values...")

            val success = withContext(Dispatchers.IO) {
                try {
                    val sourceFile = File(projectBuilder.srcDir, "MainActivity.kt")
                    sourceFile.writeText(codeText)

                    val aaptSuccess = projectBuilder.runAapt2Compile(projectBuilder.resDir) { log -> logToTerminal(log) }
                    if (!aaptSuccess) return@withContext false

                    withContext(Dispatchers.Main) { buildState = BuildState.DexingCode }
                    logToTerminal("D8: Transforming Bytecode into dex formatting...")
                    val dexSuccess = projectBuilder.runD8Dexing(listOf(sourceFile)) { log -> logToTerminal(log) }
                    if (!dexSuccess) return@withContext false

                    withContext(Dispatchers.Main) { buildState = BuildState.SigningApk }
                    logToTerminal("ApkSigner: Aligning cryptograph and signing targets...")
                    
                    val unsignedApk = File(projectBuilder.binDir, "app-unsigned.apk").apply { if(!exists()) createNewFile() }
                    val signedApk = File(projectBuilder.binDir, "app-release.apk")

                    return@withContext apkSigner.signApk(unsignedApk, signedApk) { log -> logToTerminal(log) }
                } catch (e: Exception) {
                    logToTerminal("Error: ${e.message}")
                    false
                }
            }

            buildState = if (success) {
                logToTerminal("Success: Target production package distribution verified!")
                BuildState.Success(File(projectBuilder.binDir, "app-release.apk"))
            } else {
                BuildState.Error("Build step caught a runtime breakdown. See logs.")
            }
        }
    }

    fun resetState() { buildState = BuildState.Idle }
}
