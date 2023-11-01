package com.canopas.editor.ui.model

data class RichText(
    val text: String = "",
    val spans: MutableList<RichTextSpan> = mutableListOf()
)