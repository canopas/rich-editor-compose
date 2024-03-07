package com.canopas.editor.ui.utils

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.style.BulletSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan

sealed interface TextSpanStyle {
    val key: String
    val style: Any


    object Default : TextSpanStyle {
        override val key: String
            get() = "default"
        override val style: Any
            get() = RelativeSizeSpan(1f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }

    }

    object BoldStyle : TextSpanStyle {
        override val key: String
            get() = "bold"
        override val style: Any
            get() = StyleSpan(Typeface.BOLD)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object ItalicStyle : TextSpanStyle {
        override val key: String
            get() = "italic"
        override val style: Any
            get() = StyleSpan(Typeface.ITALIC)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object UnderlineStyle : TextSpanStyle {
        override val key: String
            get() = "underline"
        override val style: Any
            get() = UnderlineSpan()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object BulletStyle : TextSpanStyle {
        override val key: String
            get() = "bullet"
        override val style: Any
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                BulletSpan(16, Color.BLACK, 8)
            } else {
                BulletSpan(16)
            }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object H1Style : TextSpanStyle {
        override val key: String
            get() = "h1"
        override val style: Any
            get() = RelativeSizeSpan(1.5f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }


    object H2Style : TextSpanStyle {
        override val key: String
            get() = "h2"
        override val style: Any
            get() = RelativeSizeSpan(1.4f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object H3Style : TextSpanStyle {
        override val key: String
            get() = "h3"
        override val style: Any
            get() = RelativeSizeSpan(1.3f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object H4Style : TextSpanStyle {
        override val key: String
            get() = "h4"
        override val style: Any
            get() = RelativeSizeSpan(1.2f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object H5Style : TextSpanStyle {
        override val key: String
            get() = "h5"
        override val style: Any
            get() = RelativeSizeSpan(1.1f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object H6Style : TextSpanStyle {
        override val key: String
            get() = "h6"
        override val style: Any
            get() = RelativeSizeSpan(1f)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Default) return false
            return key == other.key
        }
    }

    object HeaderMap {
        internal val headerMap = mapOf(
            "1" to H1Style,
            "2" to H2Style,
            "3" to H3Style,
            "4" to H4Style,
            "5" to H5Style,
            "6" to H6Style
        )
    }
}