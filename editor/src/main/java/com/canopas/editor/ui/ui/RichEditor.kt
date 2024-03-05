package com.canopas.editor.ui.ui

import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.canopas.editor.ui.data.QuillEditorState

@Composable
fun RichEditor(
    state: QuillEditorState,
    modifier: Modifier = Modifier,
) {

    Box(modifier = modifier) {
        val context = LocalContext.current
        val editText = remember {
            EditText(context)
        }
        AndroidView(modifier = Modifier.clickable {
            editText.requestFocus()
        }, factory = {
            editText.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            editText.background = null

            editText.accessibilityDelegate = object : View.AccessibilityDelegate() {
                override fun sendAccessibilityEvent(host: View, eventType: Int) {
                    super.sendAccessibilityEvent(host, eventType)
                    if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                        state.manager.adjustSelection(
                            TextRange(editText.selectionStart, editText.selectionEnd)
                        )
                    }
                }
            }

            editText.doAfterTextChanged { editable ->
                editable?.let {
                    state.manager.onTextFieldValueChange(
                        it, TextRange(editText.selectionStart, editText.selectionEnd)
                    )
                }
            }

            state.manager.setEditable(editText.text)
            state.manager.adjustSelection(TextRange(editText.selectionStart, editText.selectionEnd))
            editText
        })
    }
}
