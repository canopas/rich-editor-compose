package com.canopas.editor.ui.data

import androidx.compose.ui.text.SpanStyle

data class RichTextSpan(
    val from: Int,
    val to: Int,
    val style: SpanStyle,
)