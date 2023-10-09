package com.canopas.editor.ui.utils

import androidx.compose.ui.text.style.TextDecoration

operator fun TextDecoration.minus(decoration: TextDecoration): TextDecoration =
    if (this == decoration)
        TextDecoration.None
    else if (decoration in this)
        if (decoration.mask == TextDecoration.LineThrough.mask) TextDecoration.Underline
        else TextDecoration.LineThrough
    else
        TextDecoration.None
