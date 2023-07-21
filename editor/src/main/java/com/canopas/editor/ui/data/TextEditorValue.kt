package com.canopas.editor.ui.data

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.canopas.editor.ui.model.RichTextStyle

@Immutable
class TextEditorValue internal constructor(internal val values: MutableList<ContentValue> = mutableListOf()) {

    init {
        if (values.isEmpty()) {
            val richTextValue = RichTextValue().apply { isFocused = true }
            values.add(richTextValue)
        }
    }

    fun getContent() = values

    fun setContent(content: List<ContentValue>) {
        values.clear()
        if (content.isNotEmpty()) {
            values.addAll(content)
        } else {
            val richTextValue = RichTextValue().apply { isFocused = true }
            values.add(richTextValue)
        }
    }

    internal fun update(value: ContentValue, index: Int): TextEditorValue {
        if (index != -1 && index < values.size) {
            values[index] = value
            return TextEditorValue(values)
        }
        return this
    }

    private fun remove(index: Int): TextEditorValue {
        if (index != -1) {
            values.removeAt(index)
        }
        return TextEditorValue(values)
    }

    private fun remove(value: ContentValue): TextEditorValue {
        values.remove(value)
        return TextEditorValue(values)
    }

    private fun add(value: ContentValue, index: Int = -1): TextEditorValue {
        if (index != -1) {
            values.add(index, value)
        } else {
            values.add(value)
        }

        return TextEditorValue(values)
    }

    internal fun setFocused(index: Int, isFocused: Boolean): TextEditorValue {
        if (index == -1 || index >= values.size) return this
        if (isFocused) clearFocus()
        values[index].isFocused = isFocused
        return TextEditorValue(values)
    }

    fun hasStyle(style: RichTextStyle): Boolean {
        return values.filter { it.isFocused && it.type == ContentType.RICH_TEXT }
            .any { (it as RichTextValue).hasStyle(style) }
    }

    private fun getRichTexts(): List<RichTextValue> =
        values.filter { it.type == ContentType.RICH_TEXT }.map { it as RichTextValue }

    fun toggleStyle(style: RichTextStyle): TextEditorValue {
        values.forEachIndexed { index, value ->
            if (value.type == ContentType.RICH_TEXT) {
                val richText = (value as RichTextValue).toggleStyle(style)
                values[index] = richText
            }
        }

        return TextEditorValue(values)
    }

    fun updateStyles(styles: Set<RichTextStyle>): TextEditorValue {
        values.forEachIndexed { index, value ->
            if (value.type == ContentType.RICH_TEXT) {
                val richText = (value as RichTextValue).updateStyles(styles)
                values[index] = richText
            }
        }

        return TextEditorValue(values)
    }

    private fun clearFocus() {
        values.forEach { it.isFocused = false }
    }

    private fun focusedRichText() = getRichTexts().firstOrNull { it.isFocused }

    private fun addContent(contentValue: ContentValue): TextEditorValue {
        val focusedRichText = focusedRichText()

        focusedRichText?.let {
            if (focusedRichText.textFieldValue.text.isNotEmpty()) {
                return splitAndAdd(focusedRichText, contentValue)
            }
        }

        val value = values.last()
        if (value.type == ContentType.RICH_TEXT && (value as RichTextValue).text.isEmpty()) {
            return add(contentValue, values.lastIndex)
        }

        clearFocus()

        values.add(contentValue)
        return add(RichTextValue().apply { isFocused = true })
    }

    fun addImage(uri: Uri): TextEditorValue {
        val imageContentValue = ImageContentValue(uri = uri)
        return addContent(imageContentValue)
    }

    fun addVideo(uri: Uri): TextEditorValue {
        val videoContent = VideoContentValue(uri = uri)
        return addContent(videoContent)
    }

    private fun splitAndAdd(
        focusedRichText: RichTextValue,
        contentValue: ContentValue
    ): TextEditorValue {
        val cursorPosition = focusedRichText.textFieldValue.selection.end
        val index = values.indexOf(focusedRichText)
        if (cursorPosition >= 0) {
            clearFocus()
            val (value1, value2) = focusedRichText.split(cursorPosition)
            values[index] = value1
            values.add(index + 1, contentValue)
            return add(value2, index + 2)
        }

        return this
    }

    fun removeContent(index: Int): TextEditorValue {
        if (index != -1 && index < values.size) {
            return handleRemoveAndMerge(index)
        }
        return this
    }

    internal fun focusUp(index: Int): TextEditorValue {
        val upIndex = index - 1
        if (index != -1 && index < values.size) {
            values[index].isFocused = false
        }
        if (upIndex != -1 && upIndex < values.size) {
            val item = values[upIndex]
            if ((item.type == ContentType.IMAGE || item.type == ContentType.VIDEO) && item.isFocused) {
                return handleRemoveAndMerge(upIndex)
            } else {
                item.isFocused = true
            }
            return update(item, upIndex)
        }
        return this
    }

    private fun handleRemoveAndMerge(index: Int): TextEditorValue {
        val previousItem = values.elementAtOrNull(index - 1) ?: return remove(index)
        val nextItem = values.elementAtOrNull(index + 1) ?: return this
        clearFocus()
        remove(index)
        if (previousItem.type == ContentType.RICH_TEXT && nextItem.type == ContentType.RICH_TEXT) {
            if ((nextItem as RichTextValue).text.isNotEmpty()) {
                val value = (previousItem as RichTextValue).merge(nextItem)
                value.isFocused = true
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
    IMAGE, RICH_TEXT, VIDEO
}

abstract class ContentValue {
    abstract val type: ContentType
    abstract var isFocused: Boolean
}



