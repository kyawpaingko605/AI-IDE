package com.ai.ide.utils

import android.content.Context
import java.io.File
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader

class ProjectBuilder(private val context: Context) {

    private val internalStorage = context.filesDir
    val projectDir = File(internalStorage, "AI_Workspace")
    val srcDir = File(projectDir, "src/main/java")
    val resDir = File(projectDir, "src/main/res")
    val binDir = File(projectDir, "bin")

    private val binToolsDir = File(internalStorage, "bin_tools")

    init {
        // ၁။ Folder တွေ မရှိရင် ဆောက်မယ်
        if (!projectDir.exists()) projectDir.mkdirs()
        if (!srcDir.exists()) srcDir.mkdirs()
        if (!resDir.exists()) resDir.mkdirs()
        if (!binDir.exists()) binDir.mkdirs()

        // ၂။ အခြေခံ Project Template ဖိုင်တွေကို Assets ကနေ ကူးထည့်ပေးမယ်
        setupDefaultTemplate()
    }

    private fun setupDefaultTemplate() {
        try {
            // AndroidManifest.xml နေရာချခြင်း
            val manifestFile = File(projectDir, "AndroidManifest.xml")
            if (!manifestFile.exists()) {
                copyAssetFile("template/AndroidManifest.xml", manifestFile)
            }

            // activity_main.xml အတွက် layout folder ဆောက်ပြီး ကူးခြင်း
            val layoutDir = File(resDir, "layout")
            if (!layoutDir.exists()) layoutDir.mkdirs()
            val layoutFile = File(layoutDir, "activity_main.xml")
            if (!layoutFile.exists()) {
                copyAssetFile("template/activity_main.xml", layoutFile)
            }

            // strings.xml အတွက် values folder ဆောက်ပြီး ကူးခြင်း
            val valuesDir = File(resDir, "values")
            if (!valuesDir.exists()) valuesDir.mkdirs()
            val stringsFile = File(valuesDir, "strings.xml")
            if (!stringsFile.exists()) {
                copyAssetFile("template/strings.xml", stringsFile)
            }

            // MainActivity.kt အတွက် package folder ဆောက်ပြီး ကူးခြင်း
            val packageDir = File(srcDir, "com/ai/ide")
            if (!packageDir.exists()) packageDir.mkdirs()
            val mainActivityFile = File(packageDir, "MainActivity.kt")
            if (!mainActivityFile.exists()) {
                copyAssetFile("template/MainActivity.kt", mainActivityFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyAssetFile(assetPath: String, targetFile: File) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun runAapt2Compile(resFile: File, onLogReceived: (String) -> Unit): Boolean {
        val outputDir = File(binDir, "res_compiled")
        if (!outputDir.exists()) outputDir.mkdirs()
        
        val aapt2Binary = File(binToolsDir, "aapt2")
        val commandList = listOf(
            aapt2Binary.absolutePath, "compile",
            "--dir", resFile.absolutePath,
            "-o", outputDir.absolutePath
        )
        return executeNativeCommand(aapt2Binary, commandList, onLogReceived)
    }

    fun runD8Dexing(classFiles: List<File>, onLogReceived: (String) -> Unit): Boolean {
        val dexOutputFile = File(binDir, "classes.dex")
        val d8Binary = File(binToolsDir, "d8")
        
        val commandList = mutableListOf(d8Binary.absolutePath, "--output", dexOutputFile.absolutePath)
        classFiles.forEach { commandList.add(it.absolutePath) }
        
        return executeNativeCommand(d8Binary, commandList, onLogReceived)
    }

    private fun executeNativeCommand(binaryFile: File, command: List<String>, onLogReceived: (String) -> Unit): Boolean {
        if (!binaryFile.exists()) {
            onLogReceived("Error: ${binaryFile.name} Binary Tool ကို မတွေ့ရှိပါ။")
            return false
        }
        return try {
            binaryFile.setExecutable(true, false)
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                onLogReceived(line ?: "")
            }
            process.waitFor() == 0
        } catch (e: Exception) {
            onLogReceived("Exception: ${e.message}")
            false
        }
    }
}
