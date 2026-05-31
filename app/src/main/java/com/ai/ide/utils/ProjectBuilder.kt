package com.ai.ide.utils

import android.content.Context
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

class ProjectBuilder(private val context: Context) {

    private val internalStorage = context.filesDir
    val projectDir = File(internalStorage, "AI_Workspace")
    val srcDir = File(projectDir, "src/main/java")
    val resDir = File(projectDir, "src/main/res")
    val binDir = File(projectDir, "bin")

    private val binToolsDir = File(internalStorage, "bin_tools")

    init {
        if (!projectDir.exists()) projectDir.mkdirs()
        if (!srcDir.exists()) srcDir.mkdirs()
        if (!resDir.exists()) resDir.mkdirs()
        if (!binDir.exists()) binDir.mkdirs()
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
