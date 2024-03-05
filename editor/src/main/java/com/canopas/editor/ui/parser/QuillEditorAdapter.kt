package com.canopas.editor.ui.parser

import com.canopas.editor.ui.model.QuillSpan

interface QuillEditorAdapter {
    fun encode(input: String): QuillSpan
    fun decode(editorValue: QuillSpan): String
}

class QuillDefaultAdapter : QuillEditorAdapter {
    override fun encode(input: String): QuillSpan {
        return QuillSpan(listOf())
    }

    override fun decode(editorValue: QuillSpan): String {
        return editorValue.spans.toString()
    }
}