package com.canopas.editor.ui.model

data class RichText(
    val items: MutableList<RichTextItem> = mutableListOf()
)

data class RichTextItem(
    val type: String = "text",
    val spans: MutableList<RichTextSpan> = mutableListOf(),
    val text: String = ""
)