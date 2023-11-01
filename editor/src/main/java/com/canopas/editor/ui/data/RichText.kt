package com.canopas.editor.ui.data

data class RichText(
    val richText: String = "",
    val spans: MutableList<RichTextSpan> = mutableListOf()
)