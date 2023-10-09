package com.canopas.editor.ui.data


sealed interface EditorAttribute {
    val type: String?

    data class ImageAttribute(
        val url: String, override val type: String = "image",
    ) : EditorAttribute

    data class VideoAttribute(
        val url: String,
        override val type: String = "video",
    ) : EditorAttribute

    data class TextAttribute(
        val richText: RichTextState = RichTextState(), override val type: String = "text",
    ) : EditorAttribute {

        val isEmpty get() = richText.text.isEmpty()

        val selection get() = richText.textFieldValue.selection
    }
}
