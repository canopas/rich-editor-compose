package com.canopas.editor.ui

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle

@Composable
fun RichTextEditor(
    state: TextEditorValue,
    onValueChange: (TextEditorValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(Color.Black),
) {

    //   Log.d("XXX", "state ${state.values[0]}")

    Column(modifier) {
        state.values.forEachIndexed { index, value ->
            when (value.type) {
                ContentType.RICH_TEXT -> {
                    val richText = value as RichTextValue

                    TextFieldComponent(richText, onValueChange = {
                        onValueChange(state.update(it, index))
                    })

                }

                ContentType.IMAGE -> {

                }
            }
        }
    }
}

@Composable
fun TextFieldComponent(
    richText: RichTextValue,
    onValueChange: (RichTextValue) -> Unit
) {
    RichTextField(
        value = richText,
        onValueChange = {
            onValueChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                richText.isSelected = it.isFocused
                Log.d("XXX", "isSelected ${richText.isSelected}")

                onValueChange(richText)
            }
    )
}

@Composable
fun rememberEditorState(): MutableState<TextEditorValue> {
    return remember {
        mutableStateOf(TextEditorValue(mutableListOf(RichTextValue())))
    }
}