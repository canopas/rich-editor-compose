package com.canopas.editor.ui.parser.json

import com.canopas.editor.ui.data.RichTextSpan
import com.canopas.editor.ui.data.RichTextState
import com.canopas.editor.ui.parser.EditorParser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

internal object JsonEditorParser : EditorParser<String> {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(RichTextState::class.java, RichTextStateAdapter())
        .registerTypeAdapter(RichTextSpan::class.java, RichTextSpanAdapter())
        .create()

    override fun encode(input: String): RichTextState {
        return gson.fromJson(input, object : TypeToken<RichTextState>() {}.type)
    }

    override fun decode(editorValue: RichTextState): String {
        return gson.toJson(editorValue)
    }

}
