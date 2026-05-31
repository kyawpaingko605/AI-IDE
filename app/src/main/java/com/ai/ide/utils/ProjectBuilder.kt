package com.ai.ide.utils

import android.content.Context
import java.io.File

class ProjectBuilder(private val context: Context) {

    // AI-IDE အတွင်း သီးသန့်အလုပ်လုပ်မည့် Folder လမ်းကြောင်းများ
    private val internalStorage = context.filesDir
    val projectDir = File(internalStorage, "AI_Workspace")
    val srcDir = File(projectDir, "src/main/java")
    val resDir = File(projectDir, "src/main/res")
    val binDir = File(projectDir, "bin")
    val assetsDir = File(projectDir, "src/main/assets")

    init {
        // Folder များ မရှိသေးပါက အလိုအလျောက် ဆောက်ပေးမည့် စနစ်
        if (!projectDir.exists()) projectDir.mkdirs()
        if (!srcDir.exists()) srcDir.mkdirs()
        if (!resDir.exists()) resDir.mkdirs()
        if (!binDir.exists()) binDir.mkdirs()
        if (!assetsDir.exists()) assetsDir.mkdirs()
    }

    /**
     * 💡 [AAPT2 Task]
     * XML Resources များကို ချုံ့ပြီး compile လုပ်ရန်အတွက် Logic
     */
    fun runAapt2Compile(resFile: File): Boolean {
        val outputDir = File(binDir, "res_compiled")
        if (!outputDir.exists()) outputDir.mkdirs()
        
        // ဖုန်းထဲရှိ embedded aapt2 binary ကို လှမ်းခေါ်သည့် Command
        val command = "aapt2 compile --dir ${resFile.absolutePath} -o ${outputDir.absolutePath}"
        return executeNativeCommand(command)
    }

    /**
     * 💡 [D8 Dexer Task]
     * Java/Kotlin .class ဖိုင်များကို Android ဖတ်နိုင်သော classes.dex အဖြစ် ပြောင်းလဲခြင်း
     */
    fun runD8Dexing(classFiles: List<File>): Boolean {
        val dexOutputFile = File(binDir, "classes.dex")
        val filesList = classFiles.joinToString(" ") { it.absolutePath }
        
        val command = "d8 --output ${dexOutputFile.absolutePath} $filesList"
        return executeNativeCommand(command)
    }

    // နောက်ကွယ်ကနေ Linux Compilers များကို အမိန့်ပေးခိုင်းစေသည့် စနစ်
    private fun executeNativeCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            exitCode == 0 // 0 ပြန်လာပါက လုပ်ငန်းစဉ် အောင်မြင်သည်
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
