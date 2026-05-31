package com.ai.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TerminalView(logs: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1)
    }

    Column(modifier = modifier.fillMaxWidth().height(180.dp).background(Color(0xFF181818)).padding(8.dp)) {
        Text(text = "TERMINAL OUTPUT", fontSize = 11.sp, color = Color(0xFF00FF00), fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 6.dp))
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                val textColor = when {
                    log.contains("Error", true) || log.contains("failed", true) -> Color(0xFFF44336)
                    log.contains("Success", true) || log.contains("successfully", true) -> Color(0xFF4CAF50)
                    log.contains("AAPT2:", true) || log.contains("D8:", true) -> Color(0xFF00BCD4)
                    else -> Color(0xFFD4D4D4)
                }
                Text(text = log, fontSize = 13.sp, color = textColor, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}
