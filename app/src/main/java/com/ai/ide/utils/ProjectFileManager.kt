package com.ai.ide.utils

import java.io.File

class ProjectFileManager {

    /**
     * 📁 [Create New File/Folder Task]
     * ပရောဂျက်အတွင်း ဖိုင်အသစ် သို့မဟုတ် ဖိုဒါအသစ်များကို လမ်းကြောင်းအလိုက် ဆောက်ပေးမည့်စနစ်
     */
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

    /**
     * 🗑️ [Delete File/Folder Task]
     * ရွေးချယ်လိုက်သော ဖိုင် သို့မဟုတ် ဖိုဒါတစ်ခုလုံးကို အောက်ခြေအဆင့်အထိ (Recursively) ဖျက်ပစ်မည့်စနစ်
     */
    fun deleteNode(node: File): Boolean {
        return if (node.isDirectory) {
            node.deleteRecursively()
        } else {
            node.delete()
        }
    }

    /**
     * 📑 [Get Directory Tree Task]
     * ဖိုဒါတစ်ခုအတွင်းရှိသမျှ ဖိုင်များနှင့် ဖိုဒါများစာရင်းကို UI တွင် ပြသရန် စနစ်တကျ ပြန်ထုတ်ပေးခြင်း
     */
    fun getProjectStructure(projectDir: File): List<File> {
        return projectDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.toList() ?: emptyList()
    }
}
