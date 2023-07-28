package com.canopas.editor.ui.data

enum class AttributeScope {
    EMBEDS, TEXTS
}

sealed interface EditorAttribute {
    val key: String?
    val scope: AttributeScope?

    data class ImageAttribute(val value: String) : EditorAttribute {
        override val key: String
            get() = "image"
        override val scope: AttributeScope
            get() = AttributeScope.EMBEDS
    }

    data class VideoAttribute(val value: String) : EditorAttribute {
        override val key: String
            get() = "video"
        override val scope: AttributeScope
            get() = AttributeScope.EMBEDS
    }

    data class TextAttribute(val value: RichTextValue = RichTextValue()) : EditorAttribute {
        override val key: String
            get() = "text"
        override val scope: AttributeScope
            get() = AttributeScope.TEXTS

        val isEmpty get() = value.text.isEmpty()

        val selection get() = value.textFieldValue.selection
    }
}
