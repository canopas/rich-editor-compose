package com.canopas.editor.ui.utils

import android.text.Editable

internal inline fun <reified T> Editable.removeSpans() {
    val allSpans = getSpans(0, length, T::class.java)
    for (span in allSpans) {
        removeSpan(span)
    }
}
