package com.canopas.editor.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.canopas.editor.ui.data.RichTextPart
import com.canopas.editor.ui.data.RichTextValue


fun RichTextValue.split(cursorPosition: Int): Pair<RichTextValue, RichTextValue> {
    if (cursorPosition == -1) throw RuntimeException("cursorPosition should be >= 0")

    val copyParts = ArrayList(parts)
    val subtext1 = text.substring(0, cursorPosition)
    val subtext2 = text.substring(cursorPosition)
    val textParts1 = removeParts(parts, cursorPosition).toMutableList()

    this.parts.clear()
    this.parts.addAll(textParts1)
    val textFieldValue = TextFieldValue(subtext1, selection = TextRange(subtext1.length))
    this.updateAnnotatedString(textFieldValue)


    val newList = forwardParts(copyParts, cursorPosition).toMutableList()
    val textValue2 =
        RichTextValue(text = subtext2, currentStyles.toMutableSet(), parts = newList)
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
            RichTextPart(updatedFromIndex, updatedToIndex, textPart.styles)
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
            RichTextPart(updatedFromIndex, updatedToIndex, textPart.styles)
        }
}

