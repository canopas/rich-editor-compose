package com.canopas.editor.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

interface RichTextStyle {
    fun applyStyle(spanStyle: SpanStyle): SpanStyle
    fun isTitleStyles(): Boolean

    object Bold : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(fontWeight = FontWeight.Bold)

        }

        override fun isTitleStyles() = false
    }

    object TITLE : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.sp
            )
        }

        override fun isTitleStyles() = true
    }

    object SUB_TITLE : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0XFF666666),
                letterSpacing = 0.15.sp
            )
        }

        override fun isTitleStyles() = true

    }

    object H1 : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
        }

        override fun isTitleStyles() = true

    }

    object H2 : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }

        override fun isTitleStyles() = true
    }

    object H3 : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }

        override fun isTitleStyles() = true
    }

    object H4 : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        override fun isTitleStyles() = true
    }

    object H5 : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        override fun isTitleStyles() = true
    }


    object H6 : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        override fun isTitleStyles() = true
    }


    object Italic : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(fontStyle = FontStyle.Italic)
        }

        override fun isTitleStyles() = false
    }

    object Underline : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                textDecoration = if (
                    spanStyle.textDecoration == TextDecoration.LineThrough ||
                    spanStyle.textDecoration == TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                ) {
                    TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                } else {
                    TextDecoration.Underline
                }
            )
        }

        override fun isTitleStyles() = false
    }

    data class FontSize(val fontSize: TextUnit) : RichTextStyle {
        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
            return spanStyle.copy(
                fontSize = fontSize,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal
            )
        }

        override fun isTitleStyles() = false
    }
}
