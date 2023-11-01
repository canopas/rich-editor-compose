package com.canopas.editor.ui.parser

import com.canopas.editor.ui.data.RichEditorState

internal interface EditorParser<String> {

    fun encode(input: String): RichEditorState

    fun decode(editorValue: RichEditorState): kotlin.String

}