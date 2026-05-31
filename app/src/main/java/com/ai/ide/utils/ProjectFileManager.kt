package com.ai.ide.utils

import java.io.File

class ProjectFileManager {

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

    fun deleteNode(node: File): Boolean {
        return if (node.isDirectory) {
            node.deleteRecursively()
        } else {
            node.delete()
        }
    }

    fun getProjectStructure(projectDir: File): List<File> {
        return projectDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.toList() ?: emptyList()
    }
}
