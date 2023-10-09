package com.canopas.editor.ui.parser

import com.canopas.editor.ui.data.RichEditorState

internal interface EditorParser<T> {

    fun encode(input: T): RichEditorState

    fun decode(editorValue: RichEditorState): T

}