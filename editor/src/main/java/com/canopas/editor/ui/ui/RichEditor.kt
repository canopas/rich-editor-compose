package com.canopas.editor.ui.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.canopas.editor.ui.data.RichTextState

@Composable
fun RichEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        RichTextField(
            value = state,
            modifier = Modifier
                .fillMaxWidth()
        )
    }

}

@Composable
fun rememberEditorState(): RichTextState {
    return remember { RichTextState() }
}