package com.canopas.editor.ui.data

import androidx.compose.runtime.Immutable
import com.canopas.editor.ui.data.EditorAttribute.ImageAttribute
import com.canopas.editor.ui.data.EditorAttribute.TextAttribute
import com.canopas.editor.ui.data.EditorAttribute.VideoAttribute

@Immutable
class TextEditorValue internal constructor(internal val values: MutableList<EditorAttribute> = mutableListOf()) {

    var focusedAttributeIndex = -1

    init {
        if (values.isEmpty()) {
            focusedAttributeIndex = 0
            values.add(TextAttribute())
        }
    }

    fun getContent() = values

    fun setContent(content: List<EditorAttribute>) {
        values.clear()
        if (content.isNotEmpty()) {
            values.addAll(content)
        } else {
            values.add(TextAttribute())
        }
    }

    internal fun update(value: EditorAttribute, index: Int): TextEditorValue {
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

    private fun remove(value: EditorAttribute): TextEditorValue {
        values.remove(value)
        return TextEditorValue(values)
    }

    private fun add(value: EditorAttribute, index: Int = -1): TextEditorValue {
        if (index != -1) {
            values.add(index, value)
        } else {
            values.add(value)
        }

        return TextEditorValue(values)
    }

    internal fun setFocused(index: Int, isFocused: Boolean): TextEditorValue {
        if (index == -1 || index >= values.size) return this
        if (isFocused && focusedAttributeIndex == index) return this

        if (isFocused) focusedAttributeIndex = index
        else if (focusedAttributeIndex == index) focusedAttributeIndex = -1
        return TextEditorValue(values)
    }

    fun hasStyle(style: RichTextAttribute): Boolean {
        return values.filterIndexed { index, value ->
            focusedAttributeIndex == index && value.scope == AttributeScope.TEXTS
        }.any { (it as TextAttribute).value.hasStyle(style) }
    }

    private fun getRichTexts(): List<TextAttribute> =
        values.filter { it.scope == AttributeScope.TEXTS }.map { it as TextAttribute }

    fun toggleStyle(style: RichTextAttribute): TextEditorValue {
        values.forEachIndexed { index, value ->
            if (value.scope == AttributeScope.TEXTS) {
                val richText = ((value as TextAttribute).value).toggleStyle(style)
                values[index] = TextAttribute(richText)
            }
        }

        return TextEditorValue(values)
    }

    fun updateStyles(styles: Set<RichTextAttribute>): TextEditorValue {
        values.forEachIndexed { index, value ->
            if (value.scope == AttributeScope.TEXTS) {
                val richText = ((value as TextAttribute).value).updateStyles(styles)
                values[index] = TextAttribute(richText)
            }
        }

        return TextEditorValue(values)
    }

    private fun clearFocus() {
        focusedAttributeIndex = -1
    }

    private fun focusedAttribute() = values.elementAtOrNull(focusedAttributeIndex)

    private fun addContent(attribute: EditorAttribute): TextEditorValue {
        val focusedAttribute = focusedAttribute()

        (focusedAttribute as? TextAttribute)?.let {
            if (!it.isEmpty) {
                return splitAndAdd(it, attribute)
            }
        }

        val value = values.last()
        if (value.scope == AttributeScope.TEXTS && (value as TextAttribute).isEmpty) {
            return add(attribute, values.lastIndex)
        }

        clearFocus()

        values.add(attribute)
        return add(TextAttribute())
    }

    fun addImage(path: String): TextEditorValue {
        return addContent(ImageAttribute(path))
    }

    fun addVideo(path: String): TextEditorValue {
        return addContent(VideoAttribute(path))
    }

    private fun splitAndAdd(
        textAttribute: TextAttribute,
        newAttribute: EditorAttribute
    ): TextEditorValue {
        val cursorPosition = textAttribute.selection.end
        val index = values.indexOf(textAttribute)
        if (cursorPosition >= 0) {
            clearFocus()
            val (value1, value2) = textAttribute.value.split(cursorPosition)
            values[index] = TextAttribute(value1)
            values.add(index + 1, newAttribute)
            return add(TextAttribute(value2), index + 2)
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
            focusedAttributeIndex = -1
        }
        if (upIndex != -1 && upIndex < values.size) {
            val item = values[upIndex]
            if (item.scope == AttributeScope.EMBEDS && upIndex == focusedAttributeIndex) {
                return handleRemoveAndMerge(upIndex)
            } else {
                focusedAttributeIndex = upIndex
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
        if (previousItem.scope == AttributeScope.TEXTS && nextItem.scope == AttributeScope.TEXTS) {
            if (!(nextItem as TextAttribute).isEmpty) {
                (previousItem as TextAttribute).value.merge(nextItem.value)
            } else {
                focusedAttributeIndex = index - 1
                update(previousItem, index - 1)
            }
            focusedAttributeIndex = index - 1
            update(previousItem, index - 1)
            remove(nextItem)
        }

        return TextEditorValue(values)
    }
}



