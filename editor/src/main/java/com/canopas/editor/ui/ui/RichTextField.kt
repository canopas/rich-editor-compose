package com.canopas.editor.ui.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.canopas.editor.ui.data.RichTextState


@SuppressLint("ClickableViewAccessibility")
@Composable
internal fun RichTextField(
    value: RichTextState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(Color.Black),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() }
) {
    Column {
        AndroidView(modifier = Modifier.fillMaxSize(),
            factory = {
                val editText = EditText(it)
                Log.d("XXX", "setup edit text")

                editText.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                editText.background = null

//                editText.setOnTouchListener { v, event ->
//                    Log.d("XXX", "setOnTouchListener selection  start ${editText.selectionStart} end ${editText.selectionEnd}")
//                    return@setOnTouchListener false
//                }

                editText.accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun sendAccessibilityEvent(host: View, eventType: Int) {
                        super.sendAccessibilityEvent(host, eventType)
                        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                            value.adjustSelection(
                                TextRange(editText.selectionStart, editText.selectionEnd)
                            )
//                            Log.d(
//                                "XXX",
//                                "setAccessibilityDelegate selection  start ${editText.selectionStart} end ${editText.selectionEnd}"
//                            )

                        }
                    }
                }

                editText.doAfterTextChanged { editable ->
                    //Log.d("XXXX", "doAfterTextChanged")
                    editable?.let {
                        value.onTextFieldValueChange(
                            it,
                            TextRange(editText.selectionStart, editText.selectionEnd)
                        )
                    }
                }

                value.setEditable(editText.text)
                value.adjustSelection(TextRange(editText.selectionStart, editText.selectionEnd))
                editText
            })
    }
}