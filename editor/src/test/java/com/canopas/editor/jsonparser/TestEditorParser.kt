package com.canopas.editor.jsonparser

import com.canopas.editor.ui.model.QuillSpan
import com.canopas.editor.ui.model.Span
import com.canopas.editor.ui.parser.QuillEditorAdapter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class TestEditorParser : QuillEditorAdapter {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Span::class.java, RichTextStateAdapter())
        .create()

    override fun encode(input: String): QuillSpan {
        return gson.fromJson(input, object : TypeToken<QuillSpan>() {}.type)
    }

    override fun decode(editorValue: QuillSpan): String {
        return gson.toJson(editorValue)
    }
}
