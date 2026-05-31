package com.zyntraai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll // 🛠️ import2. အမှားအား ပြင်ဆင်ပြီးသားဖြစ်သည်
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

// IDE Dark Theme Color Palette
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

    // စာကြောင်းရေအတွက် တွက်ချက်ခြင်း
    val lineCount = codeValue.text.split("\n").size

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(EditorBg)
    ) {
        // ၁။ စာကြောင်းရေတွက်ပြသည့် ကော်လံ (Line Number Bar)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(LineNumberBar)
                .verticalScroll(verticalScrollState)
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = i.toString(),
                    color = LineNumberText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        // ၂။ ကုဒ်ရေးသည့် နေရာ (Code Input Field + Smart Logic ဦးနှောက်ပိုင်း ပေါင်းစပ်ပြီးသား)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            BasicTextField(
                value = codeValue,
                onValueChange = { newValue ->
                    val oldText = codeValue.text
                    val newText = newValue.text
                    val selectionStart = newValue.selection.start

                    // [🧠 Logic A] ကွင်းစ ကွင်းပိတ်များ အလိုအလျောက် ဖြည့်ပေးသည့် စနစ် (Auto-Pairing)
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
                            val autoPairedText = StringBuilder(newText)
                                .insert(selectionStart, pairChar)
                                .toString()
                            
                            val highlighted = highLightKotlinCode(autoPairedText)
                            codeValue = newValue.copy(
                                text = autoPairedText,
                                annotatedString = highlighted,
                                selection = androidx.compose.ui.text.TextRange(selectionStart)
                            )
                            onCodeChange(autoPairedText)
                            return@BasicTextField
                        }
                    }

                    // [🧠 Logic B] Enter ခေါက်လျှင် Space အလိုအလျောက်ချပေးသည့် စနစ် (Smart Indentation)
                    if (newText.length > oldText.length && selectionStart > 0 && newText[selectionStart - 1] == '\n') {
                        val lines = newText.substring(0, selectionStart - 1).split("\n")
                        val lastLine = lines.lastOrNull() ?: ""
                        
                        val spacesCount = lastLine.takeWhile { it == ' ' }.length
                        val extraSpaces = if (lastLine.trim().endsWith("{")) 4 else 0
                        val totalSpaces = spacesCount + extraSpaces

                        if (totalSpaces > 0) {
                            val indentSpaces = " ".repeat(totalSpaces)
                            val indentedText = StringBuilder(newText)
                                .insert(selectionStart, indentSpaces)
                                .toString()

                            val highlighted = highLightKotlinCode(indentedText)
                            codeValue = newValue.copy(
                                text = indentedText,
                                annotatedString = highlighted,
                                selection = androidx.compose.ui.text.TextRange(selectionStart + totalSpaces)
                            )
                            onCodeChange(indentedText)
                            return@BasicTextField
                        }
                    }

                    // ပုံမှန်စာရိုက်လျှင် Highlighting Engine သို့ တိုက်ရိုက်သွားမည်
                    val highlighted = highLightKotlinCode(newText)
                    codeValue = newValue.copy(annotatedString = highlighted)
                    onCodeChange(newText)
                },
                textStyle = LocalTextStyle.current.copy(
                    color = CodeDefaultText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        }
    }
}

// 💡 အဆင့်မြင့် Regex သုံး Syntax Highlighting Engine
fun highLightKotlinCode(text: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        val keywords = Pattern.compile("\\b(package|import|class|interface|fun|val|var|return|if|else|for|while|when|is|in|as|try|catch|finally|init|this|super)\\b")
        val strings = Pattern.compile("\"([^\"]*)\"|'([^']*)'")
        val comments = Pattern.compile("//.*|/\\*(?s).*?\\*/")
        val numbers = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
        val annotations = Pattern.compile("@\\w+")

        val commentMatcher = comments.matcher(text)
        while (commentMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFF6A9955)), commentMatcher.start(), commentMatcher.end())
        }

        val keywordMatcher = keywords.matcher(text)
        while (keywordMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFF569CD6)), keywordMatcher.start(), keywordMatcher.end())
        }

        val stringMatcher = strings.matcher(text)
        while (stringMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFFCE9178)), stringMatcher.start(), stringMatcher.end())
        }

        val numberMatcher = numbers.matcher(text)
        while (numberMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFFB5CEA8)), numberMatcher.start(), numberMatcher.end())
        }

        val annotationMatcher = annotations.matcher(text)
        while (annotationMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFFBBB529)), annotationMatcher.start(), annotationMatcher.end())
        }
    }
}
