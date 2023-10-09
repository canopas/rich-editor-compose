package com.canopas.editor.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

val DefaultSpanStyle = SpanStyle(fontSize = 14.sp)

val BoldSpanStyle = SpanStyle(fontWeight = FontWeight.Bold)
val ItalicSpanStyle = SpanStyle(fontStyle = FontStyle.Italic)
val UnderlineSpanStyle = SpanStyle(textDecoration = TextDecoration.Underline)

val H1SPanStyle = SpanStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black)
val H2SPanStyle = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
val H3SPanStyle = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
val H4SPanStyle = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
val H5SPanStyle = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
val H6SPanStyle = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)