package com.canopas.editor.ui.data

const val TYPE_IMAGE = "image"
const val TYPE_VIDEO = "video"
const val TYPE_TEXT = "text"


sealed interface EditorAttribute {
    val type: String?

    data class ImageAttribute(
        val url: String, override val type: String = TYPE_IMAGE,
    ) : EditorAttribute

    data class VideoAttribute(
        val url: String,
        override val type: String = TYPE_VIDEO,
    ) : EditorAttribute

    data class TextAttribute(
        val content: RichTextState = RichTextState(), override val type: String = TYPE_TEXT,
    ) : EditorAttribute {

        val isEmpty get() = content.editable.isEmpty()

        val selection get() = content.selection
    }
}
