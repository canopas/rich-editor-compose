package com.canopas.editor.ui

import androidx.compose.foundation.text.appendInlineContent
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

    fun addStyle(vararg style: RichTextStyle): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .addStyle(*style)
            .build()
    }

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

    internal fun updateTextFieldValue(newTextFieldValue: TextFieldValue): RichTextValue {
        return RichTextValueBuilder
            .from(this)
            .updateTextFieldValue(newTextFieldValue)
            .build()
    }

    fun hasStyle(style: RichTextStyle) = currentStyles.contains(style)
}
