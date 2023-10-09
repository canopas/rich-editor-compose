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
        val content: RichTextState = RichTextState(), override val type: String = "text",
    ) : EditorAttribute {

        val isEmpty get() = content.text.isEmpty()

        val selection get() = content.textFieldValue.selection
    }
}
