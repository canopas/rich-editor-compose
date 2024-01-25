package com.canopas.editor.ui.data

import com.canopas.editor.ui.model.RichText
import com.canopas.editor.ui.parser.DefaultAdapter
import com.canopas.editor.ui.parser.EditorAdapter
import com.canopas.editor.ui.utils.TextSpanStyle

class RichEditorState internal constructor(
    private val input: String,
    private val adapter: EditorAdapter = DefaultAdapter(),
) {

    internal var manager: List<RichTextManager>

    init {
        //Log.e("XXX", "Input: $input")
        manager = getRichText().items.map {
        //Log.e("XXX", "RichTextManager: $it")
            RichTextManager(it)
        }
    }

    private fun getRichText(): RichText {
        return if (input.isNotEmpty()) adapter.encode(input) else RichText()
    }

    fun output(): List<String> {
        return manager.map { adapter.decode(it.richTextItem) }
    }

    fun reset() {
        manager.forEach {
            it.reset()
        }
    }

    fun hasStyle(style: TextSpanStyle) = manager.any { it.hasStyle(style) }

    fun toggleStyle(style: TextSpanStyle) {
        manager.forEach {
            it.toggleStyle(style)
        }
    }

    fun updateStyle(style: TextSpanStyle) {
        manager.forEach {
            it.setStyle(style)
        }
    }

    class Builder {
        private var adapter: EditorAdapter = DefaultAdapter()
        private var input: String = ""

        fun setInput(input: String) = apply {
            this.input = input
        }

        fun adapter(adapter: EditorAdapter) = apply {
            this.adapter = adapter
        }

        fun build(): RichEditorState {
            return RichEditorState(input, adapter)
        }
    }
}


