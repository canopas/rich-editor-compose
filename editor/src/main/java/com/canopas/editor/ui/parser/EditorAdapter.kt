package com.canopas.editor.ui.parser

import com.canopas.editor.ui.model.RichText

interface EditorAdapter {
    fun encode(input: String): RichText
    fun decode(editorValue: RichText): String
}

class DefaultAdapter : EditorAdapter {
    override fun encode(input: String): RichText {
        return RichText("")
    }

    override fun decode(editorValue: RichText): String {
        return editorValue.text
    }
}