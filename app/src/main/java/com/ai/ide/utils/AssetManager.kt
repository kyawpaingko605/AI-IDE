package com.ai.ide.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AssetManager(private val context: Context) {

    /**
     * 💡 [Extract Binaries Task]
     * အက်ပ်အတွင်းပါဝင်မည့် aapt2 နှင့် d8 ကဲ့သို့သော Linux Binaries များကို 
     * ဖုန်းတွင်းမှ Run နိုင်ရန်အတွက် Internal Storage သို့ ကူးယူပေးမည့် စနစ်
     */
    fun extractAssetsToStorage(): Boolean {
        return try {
            val binDir = File(context.filesDir, "bin_tools")
            if (!binDir.exists()) binDir.mkdirs()

            // assets ထဲတွင် ထည့်သွင်းထားမည့် binaries စာရင်း
            val binaryFiles = listOf("aapt2", "d8")

            for (fileName in binaryFiles) {
                val outputFile = File(binDir, fileName)
                
                // ဖိုင်မရှိသေးပါက ကူးယူခြင်း လုပ်ငန်းစဉ် စတင်မည်
                if (!outputFile.exists()) {
                    context.assets.open(fileName).use { inputStream ->
                        FileOutputStream(outputFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    // 🛠️ အဓိကအချက် - ဖုန်းထဲတွင် Binary အား Execute လုပ်ခွင့် (Linux Permission) ပေးခြင်း
                    outputFile.setExecutable(true, false)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
