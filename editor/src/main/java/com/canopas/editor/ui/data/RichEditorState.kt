package com.canopas.editor.ui.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import com.canopas.editor.ui.data.EditorAttribute.ImageAttribute
import com.canopas.editor.ui.data.EditorAttribute.TextAttribute
import com.canopas.editor.ui.data.EditorAttribute.VideoAttribute
import com.canopas.editor.ui.parser.json.JsonEditorParser
import com.canopas.editor.ui.utils.split

@Immutable
class RichEditorState internal constructor(
    val attributes: MutableList<EditorAttribute> = mutableListOf()
) {


    @delegate:Transient
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

    internal fun update(value: EditorAttribute, index: Int) {
        if (index != -1 && index < attributes.size) {
            attributes[index] = value
        }
    }

    private fun remove(index: Int) {
        if (index != -1) {
            attributes.removeAt(index)
        }
    }

    private fun remove(value: EditorAttribute) {
        attributes.remove(value)
    }

    private fun add(value: EditorAttribute, index: Int = -1) {
        if (index != -1) {
            attributes.add(index, value)
        } else {
            attributes.add(value)
        }
    }

    internal fun setFocused(index: Int, isFocused: Boolean) {
        if (index == -1 || index >= attributes.size) return
        if (isFocused && focusedAttributeIndex == index) return

        if (isFocused) focusedAttributeIndexState = index
        else if (focusedAttributeIndex == index) focusedAttributeIndexState = -1
    }

    fun hasStyle(style: SpanStyle): Boolean {
        return attributes.filterIndexed { index, value ->
            focusedAttributeIndex == index && value.type == "text"
        }.any { (it as TextAttribute).content.hasStyle(style) }
    }

    private fun getRichTexts(): List<TextAttribute> =
        attributes.filter { it.type == "text" }.map { it as TextAttribute }

    fun toggleStyle(style: SpanStyle): RichEditorState {
        attributes.forEach { value ->
            if (value.type == "text") {
                ((value as TextAttribute)).content.toggleStyle(style)
            }
        }

        return this
    }

    private fun clearFocus() {
        focusedAttributeIndexState = -1
    }

    private fun focusedAttribute() = attributes.elementAtOrNull(focusedAttributeIndex)

    private fun addContent(attribute: EditorAttribute) {
        val focusedAttribute = focusedAttribute()

        (focusedAttribute as? TextAttribute)?.let {
            if (!it.isEmpty) {
                splitAndAdd(it, attribute)
                return
            }
        }

        val value = attributes.last()
        if (value.type == "text" && (value as TextAttribute).isEmpty) {
            add(attribute, attributes.lastIndex)
            return
        }

        clearFocus()

        attributes.add(attribute)
        add(TextAttribute())
    }

    fun addImage(url: String) {
        addContent(ImageAttribute(url))
    }

    fun addVideo(url: String) {
        addContent(VideoAttribute(url))
    }

    private fun splitAndAdd(
        textAttribute: TextAttribute,
        newAttribute: EditorAttribute
    ) {
        val cursorPosition = textAttribute.selection.end
        val index = attributes.indexOf(textAttribute)
        if (cursorPosition >= 0) {
            clearFocus()
            val (value1, value2) = textAttribute.content.split(cursorPosition)
            attributes[index] = TextAttribute(value1)
            attributes.add(index + 1, newAttribute)
            add(TextAttribute(value2), index + 2)
            focusedAttributeIndexState = attributes.lastIndex
        }
    }

    fun removeContent(index: Int) {
        if (index != -1 && index < attributes.size) {
            handleRemoveAndMerge(index)
        }
    }

    internal fun focusUp(index: Int) {
        val upIndex = index - 1
        if (index != -1 && index < attributes.size) {
            focusedAttributeIndexState = -1
        }
        if (upIndex != -1 && upIndex < attributes.size) {
            val item = attributes[upIndex]
            if (item.type != "text" && upIndex == focusedAttributeIndex) {
                handleRemoveAndMerge(upIndex)
                return
            } else {
                focusedAttributeIndexState = upIndex
            }
            update(item, upIndex)
        }
    }

    private fun handleRemoveAndMerge(index: Int) {
        val previousItem = attributes.elementAtOrNull(index - 1) ?: return remove(index)
        val nextItem = attributes.elementAtOrNull(index + 1) ?: return
        clearFocus()
        remove(index)
        if (previousItem.type == "text" && nextItem.type == "text") {
            if (!(nextItem as TextAttribute).isEmpty) {
                (previousItem as TextAttribute).content.merge(nextItem.content)
            } else {
                focusedAttributeIndexState = index - 1
                update(previousItem, index - 1)
            }
            focusedAttributeIndexState = index - 1
            update(previousItem, index - 1)
            remove(nextItem)
        }
    }


    fun setJson(json: String) {
        if (json.isEmpty()) {
            clearFocus()
            setContent(emptyList())
            return
        }
        val state = JsonEditorParser.encode(json)
        setContent(state.attributes)
        setFocused(state.attributes.lastIndex, true)
    }

    fun toJson(): String {
        return JsonEditorParser.decode(this)
    }

}