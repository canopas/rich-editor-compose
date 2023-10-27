package com.canopas.editor.ui.utils

import androidx.compose.ui.text.TextRange
import com.canopas.editor.ui.data.RichTextSpan
import com.canopas.editor.ui.data.RichTextState


fun RichTextState.split(cursorPosition: Int): Pair<RichTextState, RichTextState> {
    if (cursorPosition == -1) throw RuntimeException("cursorPosition should be >= 0")

    val copyParts = ArrayList(spans)
    val subtext1 = editable.substring(0, cursorPosition)
    val subtext2 = editable.substring(cursorPosition)
    val textParts1 = removeParts(spans, cursorPosition).toMutableList()

    this.spans.clear()
    this.spans.addAll(textParts1)
    this.updateText()
    this.adjustSelection(TextRange(subtext1.length))


    val newList = forwardParts(copyParts, cursorPosition).toMutableList()
    val textValue2 =
        RichTextState(richText = subtext2, spans = newList)
    return Pair(this, textValue2)
}

private fun removeParts(
    originalList: List<RichTextSpan>,
    cursorPosition: Int
): List<RichTextSpan> {
    return originalList.filter { it.from <= cursorPosition - 1 }
        .map { span ->
            val updatedFromIndex = span.from
            val updatedToIndex =
                if (cursorPosition > span.to) span.to else cursorPosition - 1
            RichTextSpan(updatedFromIndex, updatedToIndex, span.style)
        }
}


private fun forwardParts(
    originalList: List<RichTextSpan>,
    cursorPosition: Int
): List<RichTextSpan> {
    return originalList.filter { it.to >= cursorPosition }
        .map { span ->
            val updatedFromIndex =
                if (cursorPosition >= span.from) 0 else span.from - cursorPosition
            val updatedToIndex =
                span.to - cursorPosition
            RichTextSpan(updatedFromIndex, updatedToIndex, span.style)
        }
}

