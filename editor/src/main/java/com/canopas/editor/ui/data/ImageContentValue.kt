package com.canopas.editor.ui.data

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Immutable
internal data class ImageContentValue internal constructor(
    internal val tag: String = "${System.currentTimeMillis()}",
    internal val uri: Uri,
    internal val size: DpSize = DpSize(100.dp, 100.dp)
) : ContentValue() {

    fun toggleSelection() {
        isFocused = !isFocused
    }

    override val type: ContentType = ContentType.IMAGE
    override var isFocused: Boolean = false
}