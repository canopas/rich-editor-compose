package com.canopas.editor.ui.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.canopas.editor.ui.data.EditorAttribute.ImageAttribute
import com.canopas.editor.ui.data.EditorAttribute.TextAttribute
import com.canopas.editor.ui.data.EditorAttribute.VideoAttribute

@Immutable
class TextEditorValue internal constructor(val attributes: MutableList<EditorAttribute> = mutableListOf()) {

    private var focusedAttributeIndexState by mutableStateOf(-1)


    val focusedAttributeIndex: Int
        get() = focusedAttributeIndexState

    init {
        if (attributes.isEmpty()) {
            focusedAttributeIndexState = 0
            attributes.add(TextAttribute())
        }
    }

    fun getContent() = attributes

    fun setContent(content: List<EditorAttribute>) {
        attributes.clear()
        if (content.isNotEmpty()) {
            attributes.addAll(content)
        } else {
            attributes.add(TextAttribute())
        }
    }

    internal fun update(value: EditorAttribute, index: Int): TextEditorValue {
        if (index != -1 && index < attributes.size) {
            attributes[index] = value
            return this
        }
        return this
    }

    private fun remove(index: Int): TextEditorValue {
        if (index != -1) {
            attributes.removeAt(index)
        }
        return this
    }

    private fun remove(value: EditorAttribute): TextEditorValue {
        attributes.remove(value)
        return this
    }

    private fun add(value: EditorAttribute, index: Int = -1): TextEditorValue {
        if (index != -1) {
            attributes.add(index, value)
        } else {
            attributes.add(value)
        }

        return this
    }

    internal fun setFocused(index: Int, isFocused: Boolean) {
        if (index == -1 || index >= attributes.size) return
        if (isFocused && focusedAttributeIndex == index) return

        if (isFocused) focusedAttributeIndexState = index
        else if (focusedAttributeIndex == index) focusedAttributeIndexState = -1
    }

    fun hasStyle(style: RichTextAttribute): Boolean {
        return attributes.filterIndexed { index, value ->
            focusedAttributeIndex == index && value.scope == AttributeScope.TEXTS
        }.any { (it as TextAttribute).value.hasStyle(style) }
    }

    private fun getRichTexts(): List<TextAttribute> =
        attributes.filter { it.scope == AttributeScope.TEXTS }.map { it as TextAttribute }

    fun toggleStyle(style: RichTextAttribute): TextEditorValue {
        attributes.forEachIndexed { index, value ->
            if (value.scope == AttributeScope.TEXTS) {
                val richText = ((value as TextAttribute).value).toggleStyle(style)
                (attributes[index] as TextAttribute).richText.value = richText
            }
        }

        return this
    }

    fun updateStyles(styles: Set<RichTextAttribute>): TextEditorValue {
        attributes.forEachIndexed { index, value ->
            if (value.scope == AttributeScope.TEXTS) {
                val richText = ((value as TextAttribute).value).updateStyles(styles)
                (attributes[index] as TextAttribute).richText.value = richText
            }
        }

        return this
    }

    private fun clearFocus() {
        focusedAttributeIndexState = -1
    }

    private fun focusedAttribute() = attributes.elementAtOrNull(focusedAttributeIndex)

    private fun addContent(attribute: EditorAttribute): TextEditorValue {
        val focusedAttribute = focusedAttribute()

        (focusedAttribute as? TextAttribute)?.let {
            if (!it.isEmpty) {
                return splitAndAdd(it, attribute)
            }
        }

        val value = attributes.last()
        if (value.scope == AttributeScope.TEXTS && (value as TextAttribute).isEmpty) {
            return add(attribute, attributes.lastIndex)
        }

        clearFocus()

        attributes.add(attribute)
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
        val index = attributes.indexOf(textAttribute)
        if (cursorPosition >= 0) {
            clearFocus()
            val (value1, value2) = textAttribute.value.split(cursorPosition)
            attributes[index] = TextAttribute(mutableStateOf(value1))
            attributes.add(index + 1, newAttribute)
            return add(TextAttribute(mutableStateOf(value2)), index + 2)
        }

        return this
    }

    fun removeContent(index: Int): TextEditorValue {
        if (index != -1 && index < attributes.size) {
            return handleRemoveAndMerge(index)
        }
        return this
    }

    internal fun focusUp(index: Int): TextEditorValue {
        val upIndex = index - 1
        if (index != -1 && index < attributes.size) {
            focusedAttributeIndexState = -1
        }
        if (upIndex != -1 && upIndex < attributes.size) {
            val item = attributes[upIndex]
            if (item.scope == AttributeScope.EMBEDS && upIndex == focusedAttributeIndex) {
                return handleRemoveAndMerge(upIndex)
            } else {
                focusedAttributeIndexState = upIndex
            }
            return update(item, upIndex)
        }
        return this
    }

    private fun handleRemoveAndMerge(index: Int): TextEditorValue {
        val previousItem = attributes.elementAtOrNull(index - 1) ?: return remove(index)
        val nextItem = attributes.elementAtOrNull(index + 1) ?: return this
        clearFocus()
        remove(index)
        if (previousItem.scope == AttributeScope.TEXTS && nextItem.scope == AttributeScope.TEXTS) {
            if (!(nextItem as TextAttribute).isEmpty) {
                (previousItem as TextAttribute).value.merge(nextItem.value)
            } else {
                focusedAttributeIndexState = index - 1
                update(previousItem, index - 1)
            }
            focusedAttributeIndexState = index - 1
            update(previousItem, index - 1)
            remove(nextItem)
        }

        return this
    }
}