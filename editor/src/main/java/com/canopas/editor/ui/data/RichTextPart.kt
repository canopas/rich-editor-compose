package com.canopas.editor.ui.data

import androidx.compose.ui.text.SpanStyle

data class RichTextPart(
    val fromIndex: Int,
    val toIndex: Int,
    val spanStyle: SpanStyle,
)