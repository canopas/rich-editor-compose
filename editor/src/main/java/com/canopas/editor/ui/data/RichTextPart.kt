package com.canopas.editor.ui.data

import androidx.compose.runtime.snapshots.SnapshotStateList

data class RichTextPart(
    val fromIndex: Int,
    val toIndex: Int,
    val styles: SnapshotStateList<RichTextAttribute>,
)