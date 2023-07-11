package com.canopas.editor.ui.data

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
internal data class VideoContentValue internal constructor(
    internal val uri: Uri,
) : ContentValue() {

    override val type: ContentType = ContentType.VIDEO
    override var isFocused: Boolean = false
}