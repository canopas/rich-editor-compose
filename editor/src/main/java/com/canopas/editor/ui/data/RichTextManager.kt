package com.canopas.editor.ui.data

import android.text.Editable
import android.text.Spannable
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.TextRange
import com.canopas.editor.ui.model.RichText
import com.canopas.editor.ui.model.RichTextSpan
import com.canopas.editor.ui.utils.TextSpanStyle
import kotlin.math.max
import kotlin.math.min

class RichTextManager(richText: RichText) {

    private var editable: Editable = Editable.Factory().newEditable(richText.text)
    private val spans: MutableList<RichTextSpan> = richText.spans
    private val editableText: String get() = editable.toString()

    private var selection = TextRange(0, 0)
    private val currentStyles = mutableStateListOf<TextSpanStyle>()
    private var rawText: String = ""

    val richText: RichText
        get() = RichText(editableText, spans)


    init {
        updateText()
    }

    internal fun setEditable(editable: Editable) {
        this.editable = editable
    }

    private fun updateText() {
        editable.removeSpans<RelativeSizeSpan>()
        editable.removeSpans<StyleSpan>()
        editable.removeSpans<UnderlineSpan>()

        spans.forEach {
            editable.setSpan(
                it.style.style,
                it.from,
                it.to + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        updateCurrentSpanStyle()
    }

    private fun updateCurrentSpanStyle() {
        if (this.selection.collapsed && this.selection.min == 0) return
        this.currentStyles.clear()

        val currentStyles = if (selection.collapsed) {
            getRichSpanByTextIndex(textIndex = selection.min - 1)
        } else {
            getRichSpanListByTextRange(selection).distinct()
        }

        this.currentStyles.addAll(currentStyles)
    }

    private fun getRichSpanByTextIndex(textIndex: Int): List<TextSpanStyle> {
        return spans.filter { textIndex >= it.from && textIndex <= it.to }
            .map { it.style }
    }

    private fun getRichSpanListByTextRange(selection: TextRange): List<TextSpanStyle> {
        val matchingSpans = mutableListOf<TextSpanStyle>()

        for (part in spans) {
            val partRange = TextRange(part.from, part.to)
            if (selection.overlaps(partRange)) {
                part.style.let {
                    matchingSpans.add(it)
                }
            }
        }
        return matchingSpans
    }

    fun toggleStyle(style: TextSpanStyle) {
        if (currentStyles.contains(style)) {
            removeStyle(style)
        } else {
            addStyle(style)
        }
    }

    private fun removeStyle(style: TextSpanStyle) {
        if (currentStyles.contains(style)) {
            currentStyles.remove(style)
        }

        if (!selection.collapsed) {
            val fromIndex = selection.min
            val toIndex = selection.max

            val selectedParts = spans.filter { part ->
                part.from < toIndex && part.to >= fromIndex && part.style == style
            }
            removeStylesFromSelectedPart(selectedParts, fromIndex, toIndex)
            updateText()
        }
    }

    private fun addStyle(style: TextSpanStyle) {
        if (!currentStyles.contains(style)) {
            currentStyles.add(style)
        }

        if ((style.isHeaderStyle() || style.isDefault()) && selection.collapsed) {
            handleAddHeaderStyle(style)
        }

        if (!selection.collapsed) {
            applyStylesToSelectedText(style)
        }
    }

    private fun handleAddHeaderStyle(
        style: TextSpanStyle,
        text: String = rawText
    ) {
        if (text.isEmpty()) return
        val fromIndex = selection.min
        val toIndex = if (selection.collapsed) fromIndex else selection.max
        val startIndex: Int = max(0, text.lastIndexOf("\n", fromIndex - 1))
        var endIndex: Int = text.indexOf("\n", toIndex)

        if (endIndex == -1) endIndex = text.length - 1
        val selectedParts = spans.filter { part ->
            part.from < toIndex && part.to >= fromIndex && part.style.isHeaderStyle()
        }

        removeStylesFromSelectedPart(selectedParts, startIndex, endIndex)

        spans.add(
            RichTextSpan(
                from = startIndex,
                to = endIndex,
                style = style
            )
        )

        updateText()
    }

    private fun handleRemoveHeaderStyle(
        text: String = rawText
    ) {
        if (text.isEmpty()) return

        val fromIndex = selection.min
        val toIndex = selection.max

        val startIndex: Int = max(0, text.lastIndexOf("\n", fromIndex - 1))
        var endIndex: Int = text.indexOf("\n", toIndex)

        if (endIndex == -1) endIndex = text.length - 1

        val nextNewlineIndex = text.lastIndexOf("\n", startIndex)

        val parts = spans.filter { part ->
            part.from < nextNewlineIndex && part.to >= startIndex
        }
        if (parts.isEmpty() && fromIndex - 1 == nextNewlineIndex) return

        val selectedParts = spans.filter { part ->
            part.from < endIndex && part.to >= startIndex
        }

        spans.removeAll(selectedParts.filter { it.style.isHeaderStyle() })
    }

    private fun removeStylesFromSelectedPart(
        selectedParts: List<RichTextSpan>,
        fromIndex: Int, toIndex: Int
    ) {
        selectedParts.forEach { part ->
            val index = spans.indexOf(part)
            if (index !in spans.indices) return@forEach

            if (part.from < fromIndex && part.to >= toIndex) {
                spans[index] = part.copy(to = fromIndex - 1)
                spans.add(index + 1, part.copy(from = toIndex))
            } else if (part.from < fromIndex) {
                spans[index] = part.copy(to = fromIndex - 1)
            } else if (part.to > toIndex) {
                spans[index] = part.copy(from = toIndex)
            } else {
                spans.removeAt(index)
            }
        }
    }

    private fun applyStylesToSelectedText(style: TextSpanStyle) {
        if (selection.collapsed) return

        val fromIndex = selection.min
        val toIndex = selection.max

        val selectedParts = spans.filter { part ->
            part.from < toIndex && part.to >= fromIndex
        }
        val startParts = spans.filter { fromIndex - 1 in it.from..it.to }
        val endParts = spans.filter { toIndex in it.from..it.to }

        val updateToIndex: (RichTextSpan, Int) -> Unit = { part, index ->
            val partIndex = spans.indexOf(part)
            spans[partIndex] = part.copy(to = index)
        }

        val updateFromIndex: (RichTextSpan, Int) -> Unit = { part, index ->
            val partIndex = spans.indexOf(part)
            spans[partIndex] = part.copy(from = index)
        }

        if (startParts.isEmpty() && endParts.isEmpty() && selectedParts.isNotEmpty()) {
            spans.add(RichTextSpan(from = fromIndex, to = toIndex - 1, style = style))
        } else if (style in startParts.map { it.style }) {
            startParts.filter { it.style == style }.forEach { updateToIndex(it, toIndex - 1) }
        } else if (style in endParts.map { it.style }) {
            endParts.filter { it.style == style }
                .forEach { part -> updateFromIndex(part, fromIndex) }
        } else {
            spans.add(RichTextSpan(from = fromIndex, to = toIndex - 1, style = style))
        }

        updateText()
    }

    fun setStyle(style: TextSpanStyle) {
        currentStyles.clear()
        currentStyles.add(style)

        if ((style.isHeaderStyle() || style.isDefault())) {
            handleAddHeaderStyle(style)
            return
        }
        if (!selection.collapsed) {
            applyStylesToSelectedText(style)
        }
    }

    fun onTextFieldValueChange(newText: Editable, selection: TextRange) {
        this.selection = selection
        if (newText.length > rawText.length)
            handleAddingCharacters(newText)
        else if (newText.length < rawText.length)
            handleRemovingCharacters(newText)

        updateText()
        this.rawText = newText.toString()

    }

    private fun handleAddingCharacters(newValue: Editable) {
        val typedChars = newValue.length - rawText.length
        val startTypeIndex = selection.min - typedChars

        if (newValue.getOrNull(startTypeIndex) == '\n' && currentStyles.any { it.isHeaderStyle() }) {
            currentStyles.clear()
        }

        val selectedStyles = currentStyles.toMutableList()

        moveSpans(startTypeIndex, typedChars)

        val startParts = spans.filter { startTypeIndex - 1 in it.from..it.to }
        val endParts = spans.filter { startTypeIndex in it.from..it.to }
        val commonParts = startParts.intersect(endParts.toSet())

        startParts.filter { it !in commonParts }
            .forEach {
                if (selectedStyles.contains(it.style)) {
                    val index = spans.indexOf(it)
                    spans[index] = it.copy(to = it.to + typedChars)
                    selectedStyles.remove(it.style)
                }
            }

        endParts.filter { it !in commonParts }
            .forEach { processSpan(it, typedChars, startTypeIndex, selectedStyles, true) }

        commonParts.forEach { processSpan(it, typedChars, startTypeIndex, selectedStyles) }

        selectedStyles.forEach {
            spans.add(
                RichTextSpan(
                    from = startTypeIndex,
                    to = startTypeIndex + typedChars - 1,
                    style = it
                )
            )
        }
    }

    private fun processSpan(
        richTextSpan: RichTextSpan,
        typedChars: Int,
        startTypeIndex: Int,
        selectedStyles: MutableList<TextSpanStyle>,
        forward: Boolean = false
    ) {

        val newFromIndex = richTextSpan.from + typedChars
        val newToIndex = richTextSpan.to + typedChars

        val index = spans.indexOf(richTextSpan)
        if (selectedStyles.contains(richTextSpan.style)) {
            spans[index] = richTextSpan.copy(to = newToIndex)
            selectedStyles.remove(richTextSpan.style)
        } else {
            if (forward) {
                spans[index] = richTextSpan.copy(
                    from = newFromIndex,
                    to = newToIndex
                )
            } else {
                spans[index] = richTextSpan.copy(to = startTypeIndex - 1)
                spans.add(
                    index + 1, richTextSpan.copy(
                        from = startTypeIndex + typedChars,
                        to = newToIndex
                    )
                )
                selectedStyles.remove(richTextSpan.style)
            }
        }
    }

    private fun moveSpans(startTypeIndex: Int, by: Int) {
        val filteredSpans = spans.filter { it.from > startTypeIndex }

        filteredSpans.forEach {
            val index = spans.indexOf(it)
            spans[index] = it.copy(
                from = it.from + by,
                to = it.to + by,
            )
        }
    }

    private fun handleRemovingCharacters(newText: Editable) {
        if (newText.isEmpty()) {
            spans.clear()
            currentStyles.clear()
            return
        }

        val removedCharsCount = rawText.length - newText.length
        val startRemoveIndex = selection.min + removedCharsCount
        val endRemoveIndex = selection.min
        val removeRange = endRemoveIndex until startRemoveIndex

        val newLineIndex = rawText.substring(endRemoveIndex, startRemoveIndex).indexOf("\n")

        if (newLineIndex != -1) {
            handleRemoveHeaderStyle(newText.toString())
        }

        val iterator = spans.iterator()

        val partsCopy = spans.toMutableList()

        while (iterator.hasNext()) {
            val part = iterator.next()
            val index = partsCopy.indexOf(part)

            if (removeRange.last < part.from) {
                partsCopy[index] = part.copy(
                    from = part.from - removedCharsCount,
                    to = part.to - removedCharsCount
                )
            } else if (removeRange.first <= part.from && removeRange.last >= part.to) {
                // Remove the element from the copy.
                partsCopy.removeAt(index)
            } else if (removeRange.first <= part.from) {
                partsCopy[index] = part.copy(
                    from = max(0, removeRange.first),
                    to = min(newText.length, part.to - removedCharsCount)
                )
            } else if (removeRange.last <= part.to) {
                partsCopy[index] = part.copy(to = part.to - removedCharsCount)
            } else if (removeRange.first < part.to) {
                partsCopy[index] = part.copy(to = removeRange.first)
            }
        }

        spans.clear()
        spans.addAll(partsCopy)
    }

    internal fun adjustSelection(selection: TextRange) {
        if (this.selection != selection) {
            this.selection = selection
            updateCurrentSpanStyle()
        }
    }

    fun hasStyle(style: TextSpanStyle) = currentStyles.contains(style)

    fun reset() {
        spans.clear()
        this.rawText = ""
        this.editable.clear()
        updateText()
    }

    companion object {
        private fun TextRange.overlaps(range: TextRange): Boolean {
            return end > range.start && start < range.end
        }

        fun TextSpanStyle.isDefault(): Boolean {
            return this == TextSpanStyle.Default
        }

        fun TextSpanStyle.isHeaderStyle(): Boolean {
            val headers = listOf(
                TextSpanStyle.H1Style,
                TextSpanStyle.H2Style,
                TextSpanStyle.H3Style,
                TextSpanStyle.H4Style,
                TextSpanStyle.H5Style,
                TextSpanStyle.H6Style,
            )

            return headers.contains(this)
        }

        internal inline fun <reified T> Editable.removeSpans() {
            val allSpans = getSpans(0, length, T::class.java)
            for (span in allSpans) {
                removeSpan(span)
            }
        }

    }
}