package com.example.texteditor.parser

import com.canopas.editor.ui.model.QuillSpan
import com.canopas.editor.ui.model.Span
import com.canopas.editor.ui.parser.QuillEditorAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class QuillJsonEditorParser : QuillEditorAdapter {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Span::class.java, QuillRichTextStateAdapter())
        .create()

    override fun encode(input: String): QuillSpan {
        return gson.fromJson(input, object : TypeToken<QuillSpan>() {}.type)
    }

    override fun decode(editorValue: QuillSpan): String {
        return gson.toJson(editorValue)
    }
}