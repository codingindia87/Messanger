package com.codingindia.messanger.core.utils

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

@Composable
fun LinkText(
    text: String,
    color: Color,
    fontSize: TextUnit
) {
    val context = LocalContext.current

    val annotatedString = buildAnnotatedString {
        append(text)
        val urlPattern = android.util.Patterns.WEB_URL
        val matcher = urlPattern.matcher(text)

        while (matcher.find()) {
            addStyle(
                style = SpanStyle(
                    color = Color(0xFF03A9F4), // लिंक का रंग नीला
                    textDecoration = TextDecoration.Underline
                ),
                start = matcher.start(),
                end = matcher.end()
            )
            // लिंक के लिए 'Annotation' जोड़ें
            addStringAnnotation(
                tag = "URL",
                annotation = matcher.group(),
                start = matcher.start(),
                end = matcher.end()
            )
        }
    }

    ClickableText(
        text = annotatedString,
        style = TextStyle(color = color, fontSize = fontSize),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    // ब्राउज़र में लिंक खोलें
                    Conts.openCustomTab(context,annotation.item)
                }
        }
    )
}