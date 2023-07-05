package com.canopas.editor.ui

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@Immutable
class TextEditorValue internal constructor(internal val values: MutableList<ContentValue> = mutableListOf()) {
    fun update(value: RichTextValue, index: Int): TextEditorValue {
        if (index != -1) {
            values[index] = value
            Log.d("XXX", "value ${values[index]}")
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
}

enum class ContentType {
    IMAGE, RICH_TEXT
}

abstract class ContentValue {
    abstract val type: ContentType
    abstract var isSelected: Boolean
}

@Immutable
data class ImageContentValue internal constructor(
    internal val tag: String = "${System.currentTimeMillis()}",
    internal val uri: Uri
) : ContentValue() {
    override val type: ContentType = ContentType.IMAGE
    override var isSelected: Boolean = false
}

@Immutable
data class RichTextValue internal constructor(
    internal val textFieldValue: TextFieldValue,
    internal val currentStyles: MutableSet<RichTextStyle> = mutableSetOf(),
    internal val parts: MutableList<RichTextPart> = mutableListOf()
) : ContentValue() {

    constructor(
        text: String = ""
    ) : this(textFieldValue = TextFieldValue(text = text))

    override val type: ContentType = ContentType.RICH_TEXT
    override var isSelected: Boolean = false

    internal val visualTransformation
        get() = VisualTransformation {
            TransformedText(
                text = annotatedString,
                offsetMapping = OffsetMapping.Identity
            )
        }

    private val annotatedString
        get() = buildAnnotatedString {
            append(textFieldValue.text)
            parts.map { part ->
                val spanStyle = part.styles.fold(SpanStyle()) { spanStyle, richTextStyle ->
                    richTextStyle.applyStyle(spanStyle)
                }

                addStyle(
                    style = spanStyle,
                    start = part.fromIndex,
                    end = part.toIndex + 1,
                )
            }
        }

    fun toggleStyle(style: RichTextStyle): RichTextValue {
        return if (currentStyles.contains(style)) {
            removeStyle(style)
        } else {
            addStyle(style)
        }
    }

    private fun addStyle(vararg style: RichTextStyle): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .addStyle(*style)
            .build()
    }

    private fun removeStyle(vararg style: RichTextStyle): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .removeStyle(*style)
            .build()
    }

    fun updateStyles(newStyles: Set<RichTextStyle>): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .removeStyle(*this.currentStyles.toTypedArray())
            .updateStyles(newStyles)
            .build()
    }

    internal fun updateTextFieldValue(newTextFieldValue: TextFieldValue): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .updateTextFieldValue(newTextFieldValue)
            .build()
    }

    fun hasStyle(style: RichTextStyle) = currentStyles.contains(style)
}
