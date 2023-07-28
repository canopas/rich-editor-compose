package com.canopas.editor.ui.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


enum class TextAttributeScope {
    INLINE,
    HEADER
}

object RichText {
    val Bold = RichTextAttribute.BoldAttribute
    val Italic = RichTextAttribute.ItalicAttribute
    val Underline = RichTextAttribute.UnderlineAttribute
    val H1 = RichTextAttribute.H1
    val H2 = RichTextAttribute.H2
    val H3 = RichTextAttribute.H3
    val H4 = RichTextAttribute.H4
    val H5 = RichTextAttribute.H5
    val H6 = RichTextAttribute.H6
    val NormalText = RichTextAttribute.NormalText
    val Title = RichTextAttribute.TITLE
    val SubTitle = RichTextAttribute.SUB_TITLE
}

sealed interface RichTextAttribute {
    val key: String?
    val scope: TextAttributeScope?
    fun apply(style: SpanStyle): SpanStyle

    object BoldAttribute : RichTextAttribute {
        override val key: String
            get() = "bold"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.INLINE

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(fontWeight = FontWeight.Bold)
        }
    }

    object ItalicAttribute : RichTextAttribute {
        override val key: String
            get() = "italic"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.INLINE

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(fontStyle = FontStyle.Italic)
        }

    }

    object UnderlineAttribute : RichTextAttribute {
        override val key: String
            get() = "underline"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.INLINE

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                textDecoration = if (
                    style.textDecoration == TextDecoration.LineThrough ||
                    style.textDecoration == TextDecoration.combine(
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
    }

    object NormalText : RichTextAttribute {
        override fun apply(style: SpanStyle): SpanStyle {
            return SpanStyle()
        }

        override val key: String
            get() = "text"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.INLINE
    }

    object TITLE : RichTextAttribute {
        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 38.sp,
                fontWeight = FontWeight.W800,
                letterSpacing = 0.15.sp
            )
        }

        override val key: String
            get() = "title"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER
    }

    object SUB_TITLE : RichTextAttribute {
        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.W500,
                color = Color(0XFF666666),
                letterSpacing = 0.15.sp
            )
        }

        override val key: String
            get() = "sub_title"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

    }


    object H1 : RichTextAttribute {
        override val key: String
            get() = "header1"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }


    object H2 : RichTextAttribute {
        override val key: String
            get() = "header2"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    object H3 : RichTextAttribute {
        override val key: String
            get() = "header3"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    object H4 : RichTextAttribute {
        override val key: String
            get() = "header4"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }


    object H5 : RichTextAttribute {
        override val key: String
            get() = "header5"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    object H6 : RichTextAttribute {
        override val key: String
            get() = "header6"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.HEADER

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    data class FontSize(val fontSize: TextUnit) : RichTextAttribute {
        override val key: String
            get() = "font_size"
        override val scope: TextAttributeScope
            get() = TextAttributeScope.INLINE

        override fun apply(style: SpanStyle): SpanStyle {
            return style.copy(
                fontSize = fontSize,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal
            )
        }
    }

}