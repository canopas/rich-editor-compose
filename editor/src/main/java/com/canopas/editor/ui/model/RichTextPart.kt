package com.canopas.editor.ui.model

data class RichTextPart(
    val fromIndex: Int,
    val toIndex: Int,
    val styles: Set<RichTextAttribute>,
)