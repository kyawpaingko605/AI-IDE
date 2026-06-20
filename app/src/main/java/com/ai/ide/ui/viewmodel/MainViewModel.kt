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
        createFullAndroidProjectStructure()
        extractCompilerTools()
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
                if (success) {
                    logToTerminal("System: Compiler binaries (aapt2, d8) are synchronized successfully.")
                } else {
                    logToTerminal("Warning: Native compiler tools need execution permission or asset synchronization.")
                }
            }
        }
    }

    private fun createFullAndroidProjectStructure() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val javaSrcDir = File(projectBuilder.projectDir, "src/main/java/com/example/myapp").apply { if (!exists())  mkdirs() }
                val resLayoutDir = File(projectBuilder.projectDir, "src/main/res/layout").apply { if (!exists()) mkdirs() }
                val resValuesDir = File(projectBuilder.projectDir, "src/main/res/values").apply { if (!exists()) mkdirs() }
                File(projectBuilder.projectDir, "src/main/res/drawable").apply { if (!exists()) mkdirs() }
                
                if (!projectBuilder.binDir.exists()) projectBuilder.binDir.mkdirs()

                val manifestFile = File(projectBuilder.projectDir, "src/main/AndroidManifest.xml")
                if (!manifestFile.exists()) {
                    manifestFile.parentFile.mkdirs()
                    manifestFile.writeText("""
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example.myapp">
                            <application android:label="@string/app_name" android:theme="@android:style/Theme.DeviceDefault.NoActionBar">
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

                val mainLayoutFile = File(resLayoutDir, "activity_main.xml")
                if (!mainLayoutFile.exists()) {
                    mainLayoutFile.writeText("""
                        <?xml version="1.0" encoding="utf-8"?>
                        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:orientation="vertical"
                            android:gravity="center"
                            android:background="#1E1E1E">
                            
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Hello from AI-IDE Generated APK!"
                                android:textColor="#FFFFFF"
                                android:textSize="18sp"/>
                        </LinearLayout>
                    """.trimIndent())
                }

                val stringsFile = File(resValuesDir, "strings.xml")
                if (!stringsFile.exists()) {
                    stringsFile.writeText("""
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources>
                            <string name="app_name">AI IDE Demo App</string>
                        </resources>
                    """.trimIndent())
                }

                val mainActivityFile = File(javaSrcDir, "MainActivity.kt")
                if (!mainActivityFile.exists()) {
                    mainActivityFile.writeText("""
                        package com.example.myapp

                        import android.os.Bundle
                        import android.app.Activity

                        class MainActivity : Activity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                setContentView(resources.getIdentifier("activity_main", "layout", packageName))
                            }
                        }
                    """.trimIndent())
                }
                
                withContext(Dispatchers.Main) {
                    logToTerminal("Build Engine: AIDE Project templates successfully synchronized!")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logToTerminal("Build Engine Exception: ${e.message}")
                }
            }
        }
    }

    fun buildProject(codeText: String) {
        viewModelScope.launch {
            clearTerminal()
            buildState = BuildState.CompilingResources
            logToTerminal("AAPT2: Compiling assets and XML values...")

            val success = withContext(Dispatchers.IO) {
                try {
                    val javaSrcDir = File(projectBuilder.projectDir, "src/main/java/com/example/myapp")
                    val sourceFile = File(javaSrcDir, "MainActivity.kt")
                    sourceFile.writeText(codeText)

                    val resDirToCompile = File(projectBuilder.projectDir, "src/main/res")
                    val aaptSuccess = projectBuilder.runAapt2Compile(resDirToCompile) { log -> logToTerminal(log) }
                    if (!aaptSuccess) return@withContext false

                    withContext(Dispatchers.Main) { buildState = BuildState.DexingCode }
                    logToTerminal("D8: Transforming Bytecode into dex formatting...")
                    val dexSuccess = projectBuilder.runD8Dexing(listOf(sourceFile)) { log -> logToTerminal(log) }
                    if (!dexSuccess) return@withContext false

                    withContext(Dispatchers.Main) { buildState = BuildState.SigningApk }
                    logToTerminal("ApkSigner: Aligning cryptograph and signing targets...")
                    
                    val unsignedApk = File(projectBuilder.binDir, "app-unsigned.apk")
                    val signedApk = File(projectBuilder.binDir, "app-release.apk")

                    // AAPT2 ကြောင့် တကယ်ထွက်လာတဲ့ Unsigned APK ရှိမှသာ Sign ဆက်လုပ်ခိုင်းခြင်း
                    if (unsignedApk.exists()) {
                        return@withContext apkSigner.signApk(unsignedApk, signedApk) { log -> logToTerminal(log) }
                    } else {
                        withContext(Dispatchers.Main) { logToTerminal("Error: Unsigned APK not found. AAPT2 linking might have skipped output.") }
                        return@withContext false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        logToTerminal("Error: ${e.message}")
                    }
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
