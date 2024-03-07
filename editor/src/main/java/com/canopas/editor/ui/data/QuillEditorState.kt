package com.canopas.editor.ui.data

import com.canopas.editor.ui.data.QuillTextManager.Companion.headerLevel
import com.canopas.editor.ui.data.QuillTextManager.Companion.isHeaderStyle
import com.canopas.editor.ui.model.Attributes
import com.canopas.editor.ui.model.ListType
import com.canopas.editor.ui.model.QuillSpan
import com.canopas.editor.ui.model.QuillTextSpan
import com.canopas.editor.ui.model.Span
import com.canopas.editor.ui.parser.QuillDefaultAdapter
import com.canopas.editor.ui.parser.QuillEditorAdapter
import com.canopas.editor.ui.utils.TextSpanStyle

class QuillEditorState internal constructor(
    private val input: String,
    private val adapter: QuillEditorAdapter = QuillDefaultAdapter(),
) {

    internal var manager: QuillTextManager

    init {
        manager = QuillTextManager(getQuillSpan())
    }

    fun getQuillSpan(): QuillSpan {
        return if (input.isNotEmpty()) adapter.encode(input) else QuillSpan(emptyList())
    }

    fun output(): String {
        return  adapter.decode(getRichText())
    }

    fun reset() {
        manager.reset()
    }

    fun hasStyle(style: TextSpanStyle) = manager.hasStyle(style)

    fun toggleStyle(style: TextSpanStyle) {
        manager.toggleStyle(style)
    }

    fun updateStyle(style: TextSpanStyle) {
        manager.setStyle(style)
    }

    internal fun getRichText() : QuillSpan {
        val quillGroupedSpans = manager.quillTextSpans.groupBy { it.from to it.to }
        val quillTextSpans =
            quillGroupedSpans.map { (fromTo, spanList) ->
                val (from, to) = fromTo
                val uniqueStyles = spanList.map { it.style }.flatten().distinct()
                QuillTextSpan(from, to, uniqueStyles)
            }

        val groupedSpans = mutableListOf<Span>()
        quillTextSpans.forEachIndexed { index, span ->
            var insert = manager.editableText.substring(span.from, span.to + 1)
            if (insert == " " || insert == "") {
                return@forEachIndexed
            }
            val nextSpan = quillTextSpans.getOrNull(index + 1)
            val previousSpan = quillTextSpans.getOrNull(index - 1)
            val nextInsert =
                nextSpan?.let { manager.editableText.substring(nextSpan.from, nextSpan.to + 1) }
            if (nextInsert == " " || nextInsert == "") {
                insert += nextInsert
            }
            var attributes =
                Attributes(
                    header =
                    if (span.style.any { it.isHeaderStyle() })
                        span.style.find { it.isHeaderStyle() }?.headerLevel()
                    else null,
                    bold = if (span.style.contains(TextSpanStyle.BoldStyle)) true else null,
                    italic = if (span.style.contains(TextSpanStyle.ItalicStyle)) true else null,
                    underline =
                    if (span.style.contains(TextSpanStyle.UnderlineStyle)) true else null,
                    list =
                    if (span.style.contains(TextSpanStyle.BulletStyle)) ListType.bullet
                    else null
                )

            if (insert == "\n") {
                attributes = Attributes()
            }

            if (
                previousSpan?.style?.contains(TextSpanStyle.BulletStyle) == true &&
                nextInsert == "\n" &&
                !insert.contains("\n")
            ) {
                insert += "\n"
            }
            if (
                insert == "\n" &&
                span.style.contains(TextSpanStyle.BulletStyle) &&
                previousSpan?.style?.contains(TextSpanStyle.BulletStyle) == true &&
                nextSpan?.style?.contains(TextSpanStyle.BulletStyle) == true
            ) {
                return@forEachIndexed
            }
            insert = insert.replace("\u200B", "")
            // Merge consecutive spans with the same attributes into one
            if (
                groupedSpans.isNotEmpty() &&
                groupedSpans.last().attributes == attributes &&
                (attributes.list == null ||
                        (groupedSpans.last().insert?.contains('\n') == false))
            ) {
                groupedSpans.last().insert += insert
            } else {
                groupedSpans.add(Span(insert, attributes))
            }
        }

        return QuillSpan(groupedSpans)
    }

    class Builder {
        private var adapter: QuillEditorAdapter = QuillDefaultAdapter()
        private var input: String = ""

        fun setInput(input: String) = apply {
            this.input = input
        }

        fun adapter(adapter: QuillEditorAdapter) = apply {
            this.adapter = adapter
        }

        fun build(): QuillEditorState {
            return QuillEditorState(input, adapter)
        }
    }
}