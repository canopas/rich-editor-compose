package com.canopas.editor.ui

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalComposeUiApi::class)
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
    Log.d("AAA", "content size ${state.values.size}")

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
                    val imageContentValue = value as ImageContentValue
                    ImageComponent(imageContentValue, onValueChange = {
                        onValueChange(state.update(it, index))
                    })
                }
            }
        }
    }
}

@Composable
fun ImageComponent(
    contentValue: ImageContentValue,
    onValueChange: (ImageContentValue) -> Unit
) {
    AsyncImage(
        model = contentValue.uri,
        contentDescription = null,
        modifier = Modifier
            .size(contentValue.size)
            .border(1.dp, if (contentValue.isSelected) Color.Green else Color.Transparent)
            .clickable {
                contentValue.toggleSelection()
                onValueChange(contentValue)
            },
    )
}

@Composable
fun TextFieldComponent(
    richText: RichTextValue,
    onValueChange: (RichTextValue) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    RichTextField(
        value = richText,
        onValueChange = {
            onValueChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                richText.isSelected = it.isFocused
                Log.d("XXX", "isSelected ${richText.isSelected}")
                onValueChange(richText)
            }
    )

    LaunchedEffect(key1 = richText.isSelected, block = {
        if (richText.isSelected) {
            focusRequester.requestFocus()
        }
    })
}

@Composable
fun rememberEditorState(): MutableState<TextEditorValue> {
    return remember {
        mutableStateOf(TextEditorValue(mutableListOf(RichTextValue())))
    }
}