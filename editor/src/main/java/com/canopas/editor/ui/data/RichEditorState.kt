package com.canopas.editor.ui.data

import android.text.Editable
import android.text.Spannable
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.TextRange
import com.canopas.editor.ui.parser.json.JsonEditorParser
import com.canopas.editor.ui.utils.TextSpanStyle
import com.canopas.editor.ui.utils.isDefault
import com.canopas.editor.ui.utils.isHeaderStyle
import kotlin.math.max
import kotlin.math.min

class RichEditorState internal constructor(
    val richText: String = "",
    val spans: MutableList<RichTextSpan> = mutableListOf()
) {

    internal var editable: Editable = Editable.Factory().newEditable(richText)
        private set
    private var selection = TextRange(0, 0)
    private val currentStyles = mutableStateListOf<TextSpanStyle>()
    private var rawText: String = richText

    init {
        updateText()
    }

    internal fun setEditable(editable: Editable) {
        this.editable = editable
    }

    private inline fun <reified T> Editable.removeSpans() {
        val allSpans = getSpans(0, length, T::class.java)
        for (span in allSpans) {
            removeSpan(span)
        }
    }

    internal fun updateText() {
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

    private fun TextRange.overlaps(range: TextRange): Boolean {
        return end > range.start && start < range.end
    }

    fun toggleStyle(style: TextSpanStyle) {
        if (currentStyles.contains(style)) {
            removeStyle(style)
        } else {
            addStyle(style)
        }
    }

    fun updateStyle(style: TextSpanStyle) {
        currentStyles.clear()
        currentStyles.add(style)

        if ((style.isHeaderStyle() || style.isDefault())) {
            handleAddHeaderStyle(style)
            updateText()
            return
        }
        if (!selection.collapsed) {
            applyStylesToSelectedText(style)
        }
    }

    private fun addStyle(style: TextSpanStyle) {
        if (!currentStyles.contains(style)) {
            currentStyles.add(style)
        }

        if ((style.isHeaderStyle() || style.isDefault()) && selection.collapsed) {
            handleAddHeaderStyle(style)
            updateText()
        }

        if (!selection.collapsed) {
            applyStylesToSelectedText(style)
        }
    }

    private fun applyStylesToSelectedText(style: TextSpanStyle) {
        if (selection.collapsed) {
            return
        }

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


        val addPart: () -> Unit = {
            spans.add(
                RichTextSpan(
                    from = fromIndex,
                    to = toIndex - 1,
                    style = style
                )
            )
        }
        if (selectedParts.isEmpty()) {
            when {
                (startParts.isNotEmpty() && endParts.isNotEmpty()) && startParts == endParts -> {
                    startParts.forEach { part ->
                        updateToIndex(part, toIndex - 1)
                    }
                }

                style in startParts.map { it.style } -> {
                    val parts = startParts.filter { it.style == style }
                    parts.forEach { part ->
                        updateToIndex(part, toIndex - 1)
                    }
                }

                style in endParts.map { it.style } -> {
                    val parts = endParts.filter { it.style == style }
                    parts.forEach { part ->
                        updateFromIndex(part, fromIndex)
                    }
                }

                else -> {
                    addPart()
                }
            }
        } else {
            if (startParts.isEmpty() && endParts.isEmpty()) {
                addPart()
            } else if (startParts.isNotEmpty() && endParts.isNotEmpty() && startParts == endParts) {
                startParts.forEach { part ->
                    updateToIndex(part, toIndex - 1)
                }
            } else if (style in startParts.map { it.style }) {
                val parts = startParts.filter { it.style == style }
                parts.forEach { part ->
                    updateToIndex(part, toIndex - 1)
                }
            } else if (style in endParts.map { it.style }) {
                val parts = endParts.filter { it.style == style }
                parts.forEach { part ->
                    updateFromIndex(part, fromIndex)
                }
            } else {
                addPart()
            }
        }

        mergeSequentialParts()
        updateText()
    }

    private fun mergeSequentialParts() {
        if (spans.isEmpty()) return

        val result = mutableListOf<RichTextSpan>()
        val styleToMergedSegments = mutableMapOf<TextSpanStyle, RichTextSpan>()

        for (richTextPart in spans) {
            val existingSegment = styleToMergedSegments[richTextPart.style]
            if (existingSegment != null) {
                if (existingSegment.to + 1 == richTextPart.from) {
                    // Merge by creating a new instance
                    styleToMergedSegments[richTextPart.style] = RichTextSpan(
                        existingSegment.from,
                        richTextPart.to,
                        existingSegment.style
                    )
                } else if (richTextPart.to + 1 == existingSegment.from) {
                    // Merge by creating a new instance
                    styleToMergedSegments[richTextPart.style] = RichTextSpan(
                        richTextPart.from,
                        existingSegment.to,
                        existingSegment.style
                    )
                } else {
                    result.add(existingSegment)
                    styleToMergedSegments[richTextPart.style] = richTextPart
                }
            } else {
                styleToMergedSegments[richTextPart.style] = richTextPart
            }
        }

        result.addAll(styleToMergedSegments.values)
        spans.clear()
        spans.addAll(result)
    }

    private fun removeStyle(style: TextSpanStyle) {
        if (currentStyles.contains(style)) {
            currentStyles.remove(style)
        }

        if (!selection.collapsed) {
            removeStylesFromSelectedText(style)
            updateText()
        }
    }

    private fun removeStylesFromSelectedText(fromIndex: Int, toIndex: Int) {
        val selectedParts = spans.filter { part ->
            part.from < toIndex && part.to >= fromIndex
        }

        selectedParts.forEach { part ->
            val index = spans.indexOf(part)
            if (index !in spans.indices) return@forEach

            if (part.from < fromIndex && part.to >= toIndex) {
                spans[index] = part.copy(to = fromIndex - 1)
                spans.add(index + 1, part.copy(from = toIndex))
            } else if (part.from < fromIndex) {
                spans[index] = part.copy(to = fromIndex - 1)
            } else {
                spans.removeAt(index)
            }
        }
    }


    private fun removeStylesFromSelectedText(style: TextSpanStyle) {
        val fromIndex = selection.min
        val toIndex = selection.max

        val selectedParts = spans.filter { part ->
            part.from < toIndex && part.to >= fromIndex && part.style == style
        }

        selectedParts.forEach { part ->
            val index = spans.indexOf(part)
            if (index !in spans.indices) return@forEach

            if (part.from < fromIndex && part.to >= toIndex) {
                spans[index] = part.copy(to = fromIndex - 1)
                spans.add(
                    index + 1,
                    part.copy(
                        from = toIndex,
                    )
                )
            } else if (part.from < fromIndex) {
                spans[index] = part.copy(
                    to = fromIndex - 1
                )
            } else if (part.to >= toIndex) {
                spans[index] = part.copy(from = toIndex)
            } else {
                spans.removeAt(index)
            }
        }

    }

    fun onTextFieldValueChange(newText: Editable, selection: TextRange) {
        this.selection = selection
        if (newText.length > rawText.length)
            handleAddingCharacters(newText)
        else if (newText.length < rawText.length)
            handleRemovingCharacters(newText)

        collapseParts(textLastIndex = newText.lastIndex)
        updateText()
        this.rawText = newText.toString()

    }

    internal fun adjustSelection(selection: TextRange) {
        if (this.selection != selection) {
            this.selection = selection
            updateCurrentSpanStyle()
        }
    }

    private fun handleAddHeaderStyle(
        style: TextSpanStyle,
        text: String = rawText
    ) {
        val fromIndex = selection.min
        val toIndex = if (selection.collapsed) fromIndex else selection.max

        val startIndex: Int = max(0, text.lastIndexOf("\n", fromIndex - 1))
        var endIndex: Int = text.indexOf("\n", toIndex)

        if (endIndex == -1) endIndex = text.length - 1
        removeStylesFromSelectedText(startIndex, endIndex)

        spans.add(
            RichTextSpan(
                from = startIndex,
                to = endIndex,
                style = style
            )
        )
    }

    private fun handleAddingCharacters(newValue: Editable) {
        val typedChars = newValue.length - rawText.length
        val startTypeIndex = selection.min - typedChars

        if (newValue.getOrNull(startTypeIndex) == '\n') {
            removeTitleStylesIfAny()
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

    private fun removeTitleStylesIfAny() {
        if (currentStyles.any { it.isHeaderStyle() }) {
            currentStyles.clear()
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

    private fun forwardParts(
        fromIndex: Int,
        toIndex: Int,
        by: Int
    ) {
        val start = max(fromIndex, 0)
        val end = min(toIndex, spans.lastIndex)
        (start..end).forEach { index ->
            spans[index] = spans[index].copy(
                from = spans[index].from + by,
                to = spans[index].to + by,
            )
        }
    }

    private fun handleRemovingCharacters(newText: Editable) {
        val removedCharsCount = rawText.length - newText.length
        val startRemoveIndex = selection.min + removedCharsCount
        val endRemoveIndex = selection.min
        val removeRange = endRemoveIndex until startRemoveIndex

        val newLineIndex = rawText.substring(endRemoveIndex, startRemoveIndex).indexOf("\n")

        if (currentStyles.any { it.isHeaderStyle() } && newLineIndex != -1) {
            val style = currentStyles.first { it.isHeaderStyle() }
            handleAddHeaderStyle(style, newText.toString())
            return
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

    private fun collapseParts(
        textLastIndex: Int
    ) {
        val startRangeMap = mutableMapOf<Int, Int>()
        val endRangeMap = mutableMapOf<Int, Int>()
        val removedIndexes = mutableSetOf<Int>()

        val partsCopy = spans.toMutableList() // Create a copy of the original parts

        partsCopy.forEachIndexed { index, part ->
            startRangeMap[part.from] = index
            endRangeMap[part.to] = index
        }

        partsCopy.forEachIndexed { index, part ->
            if (removedIndexes.contains(index)) {
                return@forEachIndexed
            }

            val start = part.from
            val end = part.to

            if (end < start) {
                removedIndexes.add(index)
                return@forEachIndexed
            }

            if (startRangeMap.containsKey(end + 1)) {
                val otherRangeIndex = requireNotNull(startRangeMap[end + 1])
                if (partsCopy[otherRangeIndex].style == part.style) {
                    partsCopy[index] = part.copy(
                        to = partsCopy[otherRangeIndex].to
                    )

                    // Remove collapsed values
                    startRangeMap.remove(end + 1)
                    endRangeMap.remove(end)
                    removedIndexes.add(otherRangeIndex)
                }
            }

            if (endRangeMap.containsKey(start - 1)) {
                val otherRangeIndex = requireNotNull(endRangeMap[start - 1])
                if (partsCopy[otherRangeIndex].style == part.style) {
                    partsCopy[index] = part.copy(
                        from = partsCopy[otherRangeIndex].from
                    )

                    // Remove collapsed values
                    startRangeMap.remove(start - 1)
                    endRangeMap.remove(start - 1)
                    removedIndexes.add(otherRangeIndex)
                }
            }

            partsCopy[index] = partsCopy[index].copy(
                from = max(0, partsCopy[index].from),
                to = min(textLastIndex, partsCopy[index].to),
            )
        }

        removedIndexes.reversed().forEach { partsCopy.removeAt(it) }

        // Replace the original parts with the modified copy
        spans.clear()
        spans.addAll(partsCopy)
    }

    fun hasStyle(style: TextSpanStyle) = currentStyles.contains(style)

     fun reset() {
         spans.clear()
         this.rawText = ""
         this.editable.clear()
         updateText()
     }

    fun setJson(json: String) {
        if (json.isEmpty()) {
            reset()
            return
        }
        val state = JsonEditorParser.encode(json)
        reset()
        this.editable.append(state.richText)
        this.spans.addAll(state.spans)
        updateText()
    }

    fun toJson(): String {
        return JsonEditorParser.decode(this)
    }

}

