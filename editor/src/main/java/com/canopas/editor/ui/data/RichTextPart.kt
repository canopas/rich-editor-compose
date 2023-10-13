package com.canopas.editor.ui.data

import androidx.compose.ui.text.SpanStyle

data class RichTextPart(
    var fromIndex: Int,
    var toIndex: Int,
    var spanStyle: SpanStyle,
)