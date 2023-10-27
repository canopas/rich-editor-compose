package com.canopas.editor.ui.data

import com.canopas.editor.ui.utils.TextSpanStyle

data class RichTextSpan(
    val from: Int,
    val to: Int,
    val style: TextSpanStyle,
)