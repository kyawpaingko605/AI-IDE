package com.ai.ide.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class ProjectFileManager(private val context: Context) {

    init {
        // App စပွင့်တာနဲ့ aapt2, d8, apksigner တို့ကို Internal Storage ထဲ အော်တို Setup လုပ်ပေးမည်
        setupBinaries()
    }

    // ⚙️ Assets ထဲက aapt2 စသည့် Binary များကို ဖုန်းထဲ Setup လုပ်ပေးမည့် လုပ်ဆောင်ချက်
    fun setupBinaries() {
        try {
            val binToolsDir = File(context.filesDir, "bin_tools")
            if (!binToolsDir.exists()) binToolsDir.mkdirs()

            // လိုအပ်သော Binary ဖိုင်စာရင်း
            val binaries = listOf("aapt2", "d8", "apksigner")

            binaries.forEach { binaryName ->
                val targetFile = File(binToolsDir, binaryName)
                
                // ဖိုင်မရှိသေးရင် Assets ထဲကနေ ကူးထည့်မယ်
                if (!targetFile.exists()) {
                    context.assets.open("bin_tools/$binaryName").use { inputStream ->
                        FileOutputStream(targetFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
                // ဖုန်းက ပတ်မောင်းလို့ရအောင် Linux Permission (Chmod +x) ပေးခြင်း
                targetFile.setExecutable(true, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 📁 Project အသစ်ဆောက်ပြီး Template ဖိုင်များ အလိုအလျောက် ထည့်ပေးမည့် လုပ်ဆောင်ချက်
    fun createNewProject(projectDir: File, packageName: String): Boolean {
        return try {
            if (!projectDir.exists()) projectDir.mkdirs()

            // ၁။ လိုအပ်မည့် Folder ဖွဲ့စည်းပုံများကို ဆောက်မည်
            val srcDir = File(projectDir, "src/main/java/${packageName.replace('.', '/')} ")
            val resLayoutDir = File(projectDir, "src/main/res/layout")
            val resValuesDir = File(projectDir, "src/main/res/values")
            val binDir = File(projectDir, "bin")

            srcDir.mkdirs()
            resLayoutDir.mkdirs()
            resValuesDir.mkdirs()
            binDir.mkdirs()

            // ၂။ Assets ထဲက Template ဖိုင်များကို သက်ဆိုင်ရာ Folder ထဲ ကူးထည့်မည်
            copyAssetFile("template/AndroidManifest.xml", File(projectDir, "AndroidManifest.xml"))
            copyAssetFile("template/activity_main.xml", File(resLayoutDir, "activity_main.xml"))
            copyAssetFile("template/strings.xml", File(resValuesDir, "strings.xml"))
            copyAssetFile("template/MainActivity.kt", File(srcDir, "MainActivity.kt"))

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAssetFile(assetPath: String, targetFile: File) {
        context.assets.open(assetPath).use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    // ➕ ဖိုင် သို့မဟုတ် Folder အသစ်ဆောက်ခြင်း
    fun createNode(parentDir: File, name: String, isFolder: Boolean): File? {
        val newNode = File(parentDir, name)
        return try {
            if (isFolder) {
                if (newNode.mkdirs()) newNode else null
            } else {
                if (newNode.createNewFile()) newNode else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ❌ ဖိုင် သို့မဟုတ် Folder ဖျက်ခြင်း
    fun deleteNode(node: File): Boolean {
        return if (node.isDirectory) {
            node.deleteRecursively()
        } else {
            node.delete()
        }
    }

    // 🔍 Project Structure လှမ်းယူခြင်း
    fun getProjectStructure(projectDir: File): List<File> {
        return projectDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.toList() ?: emptyList()
    }
}
