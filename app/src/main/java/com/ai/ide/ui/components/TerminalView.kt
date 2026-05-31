package com.ai.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TerminalView(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFF181818))
            .padding(8.dp)
    ) {
        Text(
            text = "TERMINAL OUTPUT",
            fontSize = 11.sp,
            color = Color(0xFF00FF00),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    fontSize = 13.sp,
                    color = if (log.contains("Error", ignoreCase = true)) Color(0xFFF44336) else Color(0xFFD4D4D4),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
