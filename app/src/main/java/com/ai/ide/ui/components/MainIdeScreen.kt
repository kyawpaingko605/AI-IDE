package com.ai.ide.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ai.ide.ui.viewmodel.BuildState
import com.ai.ide.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun MainIdeScreen() {
    val context = LocalContext.current
    val viewModel = remember { MainViewModel(context) }
    val buildState = viewModel.buildState
    
    val workspaceDir = viewModel.projectBuilder.projectDir
    val defaultSourceFile = File(viewModel.projectBuilder.srcDir, "MainActivity.kt")
    
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var currentCode by remember { mutableStateOf("") }

    // Initial Code Load
    LaunchedEffect(Unit) {
        if (defaultSourceFile.exists()) {
            currentCode = defaultSourceFile.readText()
            selectedFile = defaultSourceFile
        }
    }

    LaunchedEffect(buildState) {
        when (buildState) {
            is BuildState.Success -> {
                Toast.makeText(context, "APK Production Success: ${buildState.apkFile.name}", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is BuildState.Error -> {
                Toast.makeText(context, buildState.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            if (buildState is BuildState.Idle) {
                FloatingActionButton(onClick = {
                    val fileToSave = selectedFile ?: defaultSourceFile
                    try {
                        fileToSave.writeText(currentCode)
                    } catch (e: Exception) { e.printStackTrace() }
                    viewModel.buildProject(currentCode)
                }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run Build")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFF1E1E1E))) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                FileExplorerView(
                    projectDir = workspaceDir, 
                    onFileSelected = { file ->
                        selectedFile = file
                        if (file.isFile) currentCode = file.readText()
                    },
                    modifier = Modifier.weight(0.3f)
                )

                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF3C3C3C)))

                Box(modifier = Modifier.fillMaxSize().weight(0.7f)) {
                    AdvancedCodeEditor(
                        initialCode = currentCode,
                        onCodeChange = { updatedCode -> 
                            currentCode = updatedCode
                            selectedFile?.let { try { it.writeText(updatedCode) } catch(e: Exception){} }
                        }
                    )

                    if (buildState !is BuildState.Idle) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF3C3C3C)))
            TerminalView(logs = viewModel.terminalLogs, modifier = Modifier.fillMaxWidth())
        }
    }
}
