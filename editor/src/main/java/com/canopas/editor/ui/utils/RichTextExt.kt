package com.canopas.editor.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.canopas.editor.ui.data.RichTextPart
import com.canopas.editor.ui.data.RichTextState


fun RichTextState.split(cursorPosition: Int): Pair<RichTextState, RichTextState> {
    if (cursorPosition == -1) throw RuntimeException("cursorPosition should be >= 0")

    val copyParts = ArrayList(spans)
    val subtext1 = text.substring(0, cursorPosition)
    val subtext2 = text.substring(cursorPosition)
    val textParts1 = removeParts(spans, cursorPosition).toMutableList()

    this.spans.clear()
    this.spans.addAll(textParts1)
    val textFieldValue = TextFieldValue(subtext1, selection = TextRange(subtext1.length))
    this.updateTextFieldValue(textFieldValue)


    val newList = forwardParts(copyParts, cursorPosition).toMutableList()
    val textValue2 =
        RichTextState(richText = subtext2, spans = newList)
    return Pair(this, textValue2)
}

private fun removeParts(
    originalList: List<RichTextPart>,
    cursorPosition: Int
): List<RichTextPart> {
    return originalList.filter { it.fromIndex <= cursorPosition - 1 }
        .map { textPart ->
            val updatedFromIndex = textPart.fromIndex
            val updatedToIndex =
                if (cursorPosition > textPart.toIndex) textPart.toIndex else cursorPosition - 1
            RichTextPart(updatedFromIndex, updatedToIndex, textPart.spanStyle)
        }
}


private fun forwardParts(
    originalList: List<RichTextPart>,
    cursorPosition: Int
): List<RichTextPart> {
    return originalList.filter { it.toIndex >= cursorPosition }
        .map { textPart ->
            val updatedFromIndex =
                if (cursorPosition >= textPart.fromIndex) 0 else textPart.fromIndex - cursorPosition
            val updatedToIndex =
                textPart.toIndex - cursorPosition
            RichTextPart(updatedFromIndex, updatedToIndex, textPart.spanStyle)
        }
}

