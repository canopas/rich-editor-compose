package com.canopas.editor.ui.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import com.canopas.editor.ui.model.ImageContentValue
import com.canopas.editor.ui.model.RichTextValue

@Immutable
class TextEditorValue internal constructor(internal val values: MutableList<ContentValue> = mutableListOf()) {
    fun update(value: ContentValue, index: Int): TextEditorValue {
        if (index != -1) {
            values[index] = value
            Log.d("XXX", "value ${values[index]}")
        }
        return TextEditorValue(ArrayList(values))
    }

    private fun add(value: ContentValue, index: Int = -1): TextEditorValue {
        if (index != -1) {
            values.add(index, value)
        } else {
            values.add(value)
        }
        return TextEditorValue(ArrayList(values))
    }

    fun setFocused(index: Int, isFocused: Boolean): TextEditorValue {
        if (index == -1) return this
        val richTextValue = values[index] as RichTextValue
        richTextValue.isSelected = isFocused
        return update(richTextValue, index)
    }

    fun hasStyle(style: RichTextStyle): Boolean {
        return values.filter { it.type == ContentType.RICH_TEXT }
            .any { (it as RichTextValue).hasStyle(style) }
    }

    private fun getRichTexts(): List<RichTextValue> =
        values.filter { it.type == ContentType.RICH_TEXT }.map { it as RichTextValue }


    fun toggleStyle(style: RichTextStyle): TextEditorValue {
        val index = values.indexOfFirst { it.isSelected && it.type == ContentType.RICH_TEXT }
        if (index != -1) {
            val richTextValue = values[index] as RichTextValue
            val value = richTextValue.toggleStyle(style)
            return update(value, index)
        }

        return this
    }

    fun updateStyles(styles: Set<RichTextStyle>): TextEditorValue {
        val index = values.indexOfFirst { it.isSelected && it.type == ContentType.RICH_TEXT }

        if (index != -1) {
            val richTextValue = values[index] as RichTextValue
            val value = richTextValue.updateStyles(styles)
            return update(value, index)
        }

        return this
    }

    fun addImage(uri: Uri): TextEditorValue {
        val imageContentValue = ImageContentValue(uri = uri)
        val richTextValue = RichTextValue().apply { isSelected = true }
        return add(imageContentValue).add(richTextValue)
    }
}

enum class ContentType {
    IMAGE, RICH_TEXT
}

abstract class ContentValue {
    abstract val type: ContentType
    abstract var isSelected: Boolean
}



