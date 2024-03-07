package com.canopas.editor.ui.model

import com.canopas.editor.ui.utils.TextSpanStyle

data class QuillTextSpan(
    val from: Int,
    val to: Int,
    val style: List<TextSpanStyle>,
)