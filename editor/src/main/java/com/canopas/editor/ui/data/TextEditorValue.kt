package com.canopas.editor.ui.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import com.canopas.editor.ui.model.ImageContentValue
import com.canopas.editor.ui.model.RichTextValue

@Immutable
class TextEditorValue internal constructor(internal val values: MutableList<ContentValue> = mutableListOf()) {

    init {
        Log.d("XXX", "total values ${values.size}")
    }

    fun update(value: ContentValue, index: Int): TextEditorValue {
        if (index != -1 && index < values.size) {
            values[index] = value
            return TextEditorValue(ArrayList(values))
        }
        return this
    }

    private fun remove(index: Int): TextEditorValue {
        if (index != -1) {
            values.removeAt(index)
        }
        return TextEditorValue(ArrayList(values))
    }

    private fun remove(value: ContentValue): TextEditorValue {
        values.remove(value)
        return TextEditorValue(ArrayList(values))
    }


    private fun add(value: ContentValue, index: Int = -1): TextEditorValue {
        if (index != -1) {
            values.add(index, value)
        } else {
            values.add(value)
        }

        Log.d("XXX", "add ${value.isFocused}")


        return TextEditorValue(ArrayList(values))
    }

    fun setFocused(index: Int, isFocused: Boolean): TextEditorValue {
        //   Log.d("XXX", "setFocused ${values.size}")

        if (index == -1 || index >= values.size) return this
        if (isFocused) clearFocus()
        val value = values[index]
        value.isFocused = isFocused
        return update(value, index)
    }

    fun hasStyle(style: RichTextStyle): Boolean {
        return values.filter { it.type == ContentType.RICH_TEXT }
            .any { (it as RichTextValue).hasStyle(style) }
    }

    private fun getRichTexts(): List<RichTextValue> =
        values.filter { it.type == ContentType.RICH_TEXT }.map { it as RichTextValue }

    fun toggleStyle(style: RichTextStyle): TextEditorValue {
        val index = values.indexOfFirst { it.isFocused && it.type == ContentType.RICH_TEXT }
        if (index != -1) {
            val richTextValue = values[index] as RichTextValue
            val value = richTextValue.toggleStyle(style)
            return update(value, index)
        }

        return this
    }

    fun updateStyles(styles: Set<RichTextStyle>): TextEditorValue {
        val index = values.indexOfFirst { it.isFocused && it.type == ContentType.RICH_TEXT }

        if (index != -1) {
            val richTextValue = values[index] as RichTextValue
            val value = richTextValue.updateStyles(styles)
            return update(value, index)
        }

        return this
    }

    private fun clearFocus() {
        values.forEach { it.isFocused = false }
    }

    private fun focusedRichText() = getRichTexts().firstOrNull { it.isFocused }

    fun addImage(uri: Uri): TextEditorValue {
        val imageContentValue = ImageContentValue(uri = uri)
        val richTextValue = RichTextValue().apply { isFocused = true }
        val currentIndex = values.size - 1
        val lastAddedImageIndex = values.indexOfLast { it.type == ContentType.IMAGE }

        if (lastAddedImageIndex != -1) {
            val range = values.slice(lastAddedImageIndex..currentIndex)
                .filter { it.type == ContentType.RICH_TEXT }
                .filter { (it as RichTextValue).textFieldValue.text.isEmpty() }
            values.removeAll(range)
        }

        val focusedRichText = focusedRichText()
        if (focusedRichText != null && focusedRichText.textFieldValue.text.isNotEmpty()) {
            return splitAndAdd(focusedRichText, imageContentValue)
        }

        clearFocus()

        values.add(imageContentValue)
        return add(richTextValue)
    }

    private fun splitAndAdd(
        focusedRichText: RichTextValue,
        imageContentValue: ImageContentValue
    ): TextEditorValue {
        val cursorPosition = focusedRichText.textFieldValue.selection.end
        val index = values.indexOf(focusedRichText)
        if (cursorPosition >= 0) {
            clearFocus()
            val (value1, value2) = focusedRichText.split(cursorPosition)
            values[index] = value1
            values.add(index + 1, imageContentValue)
            return add(value2, index + 2)
        }

        return this
    }

    fun focusUp(index: Int): TextEditorValue {
        val upIndex = index - 1
        if (index != -1 && index < values.size) {
            values[index].isFocused = false
        }
        if (upIndex != -1 && upIndex < values.size) {
            val item = values[upIndex]
            if (item.type == ContentType.IMAGE && item.isFocused) {
                return handleRemoveAndMerge(upIndex)
            } else {
                item.isFocused = true
            }
            return update(item, upIndex)
        }
        return this
    }

    private fun handleRemoveAndMerge(index: Int): TextEditorValue {
        val previousItem = values.elementAtOrNull(index - 1) ?: return this
        val nextItem = values.elementAtOrNull(index + 1) ?: return this

        remove(index)
        if (previousItem.type == ContentType.RICH_TEXT && nextItem.type == ContentType.RICH_TEXT) {
            if ((nextItem as RichTextValue).text.isNotEmpty()) {
                val value = (previousItem as RichTextValue).merge(nextItem)
                update(value, index - 1)
            } else {
                previousItem.isFocused = true
                update(previousItem, index - 1)
            }
            return remove(nextItem)
        }

        return this
    }
}

enum class ContentType {
    IMAGE, RICH_TEXT
}

abstract class ContentValue {
    abstract val type: ContentType
    abstract var isFocused: Boolean
}



