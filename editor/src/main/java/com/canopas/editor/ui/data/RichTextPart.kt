package com.canopas.editor.ui.data

import androidx.compose.ui.text.SpanStyle

data class RichTextPart(
    var fromIndex: Int,
    var toIndex: Int,
    var spanStyle: SpanStyle,
) {
    fun translateBy(typedChars: Int) {
        toIndex += typedChars
    }

    fun forward(by: Int) {
        fromIndex += by
        toIndex += by
    }
}