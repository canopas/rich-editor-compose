package com.canopas.editor.ui.parser.json

import com.canopas.editor.ui.data.EditorAttribute
import com.canopas.editor.ui.data.RichEditorState
import com.canopas.editor.ui.data.RichTextPart
import com.canopas.editor.ui.data.RichTextState
import com.canopas.editor.ui.parser.EditorParser
import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal object JsonEditorParser : EditorParser<String> {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(EditorAttribute::class.java, EditorAttributeAdapter())
        .registerTypeAdapter(RichTextState::class.java, RichTextStateAdapter())
        .registerTypeAdapter(RichTextPart::class.java, RichTextPartAdapter())
        .create()

    override fun encode(input: String): RichEditorState {
        return gson.fromJson(input, RichEditorState::class.java)
    }

    override fun decode(editorValue: RichEditorState): String {
        return gson.toJson(editorValue)
    }

}
