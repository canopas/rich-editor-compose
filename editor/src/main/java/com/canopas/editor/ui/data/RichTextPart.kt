package com.canopas.editor.ui.data

import androidx.compose.ui.text.SpanStyle
import com.canopas.editor.ui.parser.json.toSpansString

data class RichTextPart(
    val fromIndex: Int,
    val toIndex: Int,
    val spanStyle: SpanStyle,
) {
    fun toStr(): String {
        return "{from:$fromIndex, to:$toIndex, style:${spanStyle.toSpansString()}}"
    }
}