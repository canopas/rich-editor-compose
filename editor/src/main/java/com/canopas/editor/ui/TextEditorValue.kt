package com.canopas.editor.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.canopas.editor.ui.RichTextValueBuilder

@Immutable
data class RichTextValue internal constructor(
    internal var textFieldValue: TextFieldValue,
    internal val currentStyles: MutableSet<RichTextStyle> = mutableSetOf(),
    internal val parts: MutableList<RichTextPart> = mutableListOf()
) {
    constructor(
        text: String = ""
    ) : this(textFieldValue = TextFieldValue(text = text))

    internal val visualTransformation
        get() = VisualTransformation {
            TransformedText(
                text = annotatedString,
                offsetMapping = OffsetMapping.Identity
            )
        }

    private val annotatedString
        get() = AnnotatedString(
            text = textFieldValue.text,
            spanStyles = parts.map { part ->
                val spanStyle = part.styles.fold(SpanStyle()) { spanStyle, richTextStyle ->
                    richTextStyle.applyStyle(spanStyle)
                }

                AnnotatedString.Range(
                    item = spanStyle,
                    start = part.fromIndex,
                    end = part.toIndex + 1,
                )
            },

        )

    /**
     * Toggle a style
     * @param style the style to toggle
     * @return a new [RichTextValue] with the style toggled
     * @see RichTextStyle
     */
    fun toggleStyle(style: RichTextStyle): RichTextValue {
        return if (currentStyles.contains(style)) {
            removeStyle(style)
        } else {
            addStyle(style)
        }
    }

    /**
     * Add a style to the current styles
     * @param style the style to add
     * @return a new [RichTextValue] with the new style added
     * @see RichTextStyle
     */
    fun addStyle(vararg style: RichTextStyle): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .addStyle(*style)
            .build()
    }

    /**
     * Remove a style from the current styles
     * @param style the style to remove
     * @return a new [RichTextValue] with the style removed
     * @see RichTextStyle
     */
    fun removeStyle(vararg style: RichTextStyle): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .removeStyle(*style)
            .build()
    }

    fun setTitleStyles(newStyles: Set<RichTextStyle>): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .removeStyle(*this.currentStyles.toTypedArray())
            .updateStyles(newStyles)
            .build()
    }

    /**
     * Update the current styles
     * @param newStyles the new styles
     * @return a new [RichTextValue] with the new styles
     * @see RichTextStyle
     */
    fun updateStyles(newStyles: Set<RichTextStyle>): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .updateStyles(newStyles)
            .build()
    }

    fun clearStyles(): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .clearStyles()
            .build()
    }

    /**
     * Update the text field value and update the rich text parts accordingly to the new text field value
     * @param newTextFieldValue the new text field value
     * @return a new [RichTextValue] with the new text field value
     */
    internal fun updateTextFieldValue(newTextFieldValue: TextFieldValue): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .updateTextFieldValue(newTextFieldValue)
            .build()
    }
}
