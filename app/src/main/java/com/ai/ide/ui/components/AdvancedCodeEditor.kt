package com.ai.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

val EditorBg = Color(0xFF1E1E1E)
val LineNumberBar = Color(0xFF252526)
val LineNumberText = Color(0xFF858585)
val CodeDefaultText = Color(0xFFD4D4D4)

@Composable
fun AdvancedCodeEditor(
    modifier: Modifier = Modifier,
    initialCode: String = "",
    onCodeChange: (String) -> Unit
) {
    var codeValue by remember { mutableStateOf(TextFieldValue(initialCode)) }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    LaunchedEffect(initialCode) {
        if (initialCode != codeValue.text) {
            codeValue = TextFieldValue(text = initialCode, annotatedString = highLightKotlinCode(initialCode))
        }
    }

    val lineCount = codeValue.text.split("\n").size

    Row(modifier = modifier.fillMaxSize().background(EditorBg)) {
        Column(
            modifier = Modifier.fillMaxHeight().background(LineNumberBar).verticalScroll(verticalScrollState).padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            for (i in 1..lineCount) {
                Text(text = i.toString(), color = LineNumberText, fontFamily = FontFamily.Monospace, fontSize = 14.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().verticalScroll(verticalScrollState).horizontalScroll(horizontalScrollState).padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            BasicTextField(
                value = codeValue,
                onValueChange = { newValue ->
                    val oldText = codeValue.text
                    val newText = newValue.text
                    val selectionStart = newValue.selection.start

                    // [🧠 Logic A] Bracket Auto-Pairing
                    if (newText.length > oldText.length && selectionStart > 0) {
                        val typedChar = newText[selectionStart - 1]
                        val pairChar = when (typedChar) {
                            '{' -> '}'
                            '(' -> ')'
                            '[' -> ']'
                            '"' -> '"'
                            '\'' -> '\''
                            else -> null
                        }
                        if (pairChar != null) {
                            val autoPairedText = StringBuilder(newText).insert(selectionStart, pairChar).toString()
                            // 💡 [Fix]: copy အစား TextFieldValue ဖြင့် တိုက်ရိုက် တည်ဆောက်ခြင်း
                            codeValue = TextFieldValue(
                                text = autoPairedText,
                                selection = androidx.compose.ui.text.TextRange(selectionStart),
                                annotatedString = highLightKotlinCode(autoPairedText)
                            )
                            onCodeChange(autoPairedText)
                            return@BasicTextField
                        }
                    }

                    // [🧠 Logic B] Smart Indentation
                    if (newText.length > oldText.length && selectionStart > 0 && newText[selectionStart - 1] == '\n') {
                        val lines = newText.substring(0, selectionStart - 1).split("\n")
                        val lastLine = lines.lastOrNull() ?: ""
                        
                        val spacesCount = lastLine.takeWhile { it == ' ' }.length
                        val extraSpaces = if (lastLine.trim().endsWith("{")) 4 else 0
                        val totalSpaces = spacesCount + extraSpaces

                        if (totalSpaces > 0) {
                            val indentSpaces = " ".repeat(totalSpaces)
                            val indentedText = StringBuilder(newText).insert(selectionStart, indentSpaces).toString()
                            // 💡 [Fix]: copy အစား TextFieldValue ဖြင့် တိုက်ရိုက် တည်ဆောက်ခြင်း
                            codeValue = TextFieldValue(
                                text = indentedText,
                                selection = androidx.compose.ui.text.TextRange(selectionStart + totalSpaces),
                                annotatedString = highLightKotlinCode(indentedText)
                            )
                            onCodeChange(indentedText)
                            return@BasicTextField
                        }
                    }

                    // ပုံမှန်စာရိုက်လျှင် Highlighting Engine သို့ သွားမည်
                    codeValue = newValue.copy(annotatedString = highLightKotlinCode(newText))
                    onCodeChange(newText)
                },
                textStyle = LocalTextStyle.current.copy(color = CodeDefaultText, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        }
    }
}

fun highLightKotlinCode(text: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        val keywords = Pattern.compile("\\b(package|import|class|interface|fun|val|var|return|if|else|for|while|when|is|in|as|try|catch|finally|init|this|super)\\b")
        val strings = Pattern.compile("\"([^\"]*)\"|'([^']*)'")
        val comments = Pattern.compile("//.*|/\\*(?s).*?\\*/")
        val numbers = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")

        val keywordMatcher = keywords.matcher(text)
        while (keywordMatcher.find()) addStyle(SpanStyle(color = Color(0xFF569CD6)), keywordMatcher.start(), keywordMatcher.end())

        val numberMatcher = numbers.matcher(text)
        while (numberMatcher.find()) addStyle(SpanStyle(color = Color(0xFFB5CEA8)), numberMatcher.start(), numberMatcher.end())

        val stringMatcher = strings.matcher(text)
        while (stringMatcher.find()) addStyle(SpanStyle(color = Color(0xFFCE9178)), stringMatcher.start(), stringMatcher.end())

        val commentMatcher = comments.matcher(text)
        while (commentMatcher.find()) addStyle(SpanStyle(color = Color(0xFF6A9955)), commentMatcher.start(), commentMatcher.end())
    }
}
