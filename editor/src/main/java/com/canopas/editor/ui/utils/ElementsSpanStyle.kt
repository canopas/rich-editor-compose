package com.canopas.editor.ui.utils

import android.graphics.Typeface
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
            get() = BulletSpan(16)

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
            get() = "h3"
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
}