package com.canopas.editor.ui.ui

import android.annotation.SuppressLint
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.canopas.editor.ui.data.RichTextState


@SuppressLint("ClickableViewAccessibility")
@Composable
internal fun RichTextField(
    value: RichTextState,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    val editText = remember {
        EditText(context)
    }
    AndroidView(modifier = modifier.clickable {
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
                    value.adjustSelection(
                        TextRange(editText.selectionStart, editText.selectionEnd)
                    )
                }
            }
        }

        editText.doAfterTextChanged { editable ->
            editable?.let {
                value.onTextFieldValueChange(
                    it, TextRange(editText.selectionStart, editText.selectionEnd)
                )
            }
        }

        value.setEditable(editText.text)
        value.adjustSelection(TextRange(editText.selectionStart, editText.selectionEnd))
        editText
    })

}