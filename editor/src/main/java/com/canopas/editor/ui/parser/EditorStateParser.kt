package com.canopas.editor.ui.parser

import com.canopas.editor.ui.data.RichTextState

internal interface EditorParser<T> {

    fun encode(input: T): RichTextState

    fun decode(editorValue: RichTextState): T

}