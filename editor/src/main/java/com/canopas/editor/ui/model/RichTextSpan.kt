package com.canopas.editor.ui.model

import com.canopas.editor.ui.utils.TextSpanStyle

data class RichTextSpan(
    val from: Int,
    val to: Int,
    val style: TextSpanStyle,
)