package com.ai.ide.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.ide.utils.ProjectFileManager
import java.io.File

@Composable
fun FileExplorerView(
    projectDir: File,
    onFileSelected: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val fileManager = remember { ProjectFileManager() }
    var fileList by remember { mutableStateOf(emptyList<File>()) }

    // ဖိုင်စာရင်းများကို ဖတ်ယူခြင်း
    LaunchedEffect(projectDir) {
        fileList = fileManager.getProjectStructure(projectDir)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .padding(8.dp)
    ) {
        Text(
            text = "PROJECT FILES",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn {
            items(fileList) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!file.isDirectory) onFileSelected(file) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ဖိုဒါနှင့် ဖိုင်အလိုက် Icon ခွဲပြခြင်း
                    val icon = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description
                    val iconColor = if (file.isDirectory) Color(0xFFE5A93C) else Color(0xFF3C99E5)

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = file.name,
                        fontSize = 14.sp,
                        color = Color(0xFFD4D4D4)
                    )
                }
            }
        }
    }
}
