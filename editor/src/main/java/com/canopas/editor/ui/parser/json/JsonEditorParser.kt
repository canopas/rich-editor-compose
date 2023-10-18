package com.canopas.editor.ui.parser.json

import com.canopas.editor.ui.data.EditorAttribute
import com.canopas.editor.ui.data.RichEditorState
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

    override fun encode(input: String): RichEditorState {
        val type = object : TypeToken<List<EditorAttribute>>() {}.type
        val attributes = gson.fromJson<List<EditorAttribute>>(input, type)
        return RichEditorState(attributes.toMutableList())
    }

    override fun decode(editorValue: RichEditorState): String {
        return gson.toJson(editorValue.attributes)
    }

}
