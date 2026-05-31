package com.zyntraai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import2.import androidx.compose.foundation.verticalScroll
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

        // ၂။ ကုဒ်ရေးသည့် နေရာ (Code Input Field)
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
                    // Highlighting Logic ကို နောက်ကွယ်ကနေ အမြန်ဆုံး Run စေခြင်း
                    val highlighted = highLightKotlinCode(newValue.text)
                    codeValue = newValue.copy(annotatedString = highlighted)
                    onCodeChange(newValue.text)
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

        // Token Patterns များ သတ်မှတ်ခြင်း (Keywords, Strings, Comments, Numbers)
        val keywords = Pattern.compile("\\b(package|import|class|interface|fun|val|var|return|if|else|for|while|when|is|in|as|try|catch|finally|init|this|super)\\b")
        val strings = Pattern.compile("\"([^\"]*)\"|'([^']*)'")
        val comments = Pattern.compile("//.*|/\\*(?s).*?\\*/")
        val numbers = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
        val annotations = Pattern.compile("@\\w+")

        // ၁။ Comments အရောင်ဆိုးခြင်း (အစိမ်းရောင်)
        val commentMatcher = comments.matcher(text)
        while (commentMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFF6A9955)), commentMatcher.start(), commentMatcher.end())
        }

        // ၂။ Keywords အရောင်ဆိုးခြင်း (အပြာရောင်)
        val keywordMatcher = keywords.matcher(text)
        while (keywordMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFF569CD6)), keywordMatcher.start(), keywordMatcher.end())
        }

        // ၃။ Strings အရောင်ဆိုးခြင်း (လိမ္မော်ရောင်)
        val stringMatcher = strings.matcher(text)
        while (stringMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFFCE9178)), stringMatcher.start(), stringMatcher.end())
        }

        // ၄။ Numbers အရောင်ဆိုးခြင်း (အဝါစိမ်းရောင်)
        val numberMatcher = numbers.matcher(text)
        while (numberMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFFB5CEA8)), numberMatcher.start(), numberMatcher.end())
        }

        // ၅။ Annotations (@Composable စသည်) အရောင်ဆိုးခြင်း (ခရမ်းရောင်)
        val annotationMatcher = annotations.matcher(text)
        while (annotationMatcher.find()) {
            addStyle(SpanStyle(color = Color(0xFFBBB529)), annotationMatcher.start(), annotationMatcher.end())
        }
    }
}
