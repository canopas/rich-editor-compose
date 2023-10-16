package com.canopas.editor.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.max
import kotlin.math.min

class RichTextState internal constructor(
    val richText: String = "",
    val spans: MutableList<RichTextSpan> = mutableListOf()
) {

    internal var textFieldValue by mutableStateOf(TextFieldValue(richText))
        private set

    private val currentStyles = mutableStateListOf<SpanStyle>()

    internal var visualTransformation: VisualTransformation by mutableStateOf(VisualTransformation.None)

    private var annotatedString by mutableStateOf(AnnotatedString(text = ""))

    val text get() = textFieldValue.text

    private val selection
        get() = textFieldValue.selection


    init {
        updateTextFieldValue()
    }

    internal fun updateTextFieldValue(newValue: TextFieldValue = textFieldValue) {
        if (
            newValue.text == textFieldValue.text &&
            newValue.selection != textFieldValue.selection
        ) {
            textFieldValue = newValue
        } else {
            updateAnnotatedString(newValue)
        }

        updateCurrentSpanStyle()
    }

    private fun updateAnnotatedString(newValue: TextFieldValue = textFieldValue) {
        annotatedString = buildAnnotatedString {
            append(newValue.text)
            spans.forEach { part ->
                part.style.let {
                    addStyle(
                        style = it,
                        start = part.from,
                        end = part.to + 1,
                    )
                }
            }
        }
        textFieldValue = newValue
        visualTransformation = VisualTransformation { _ ->
            TransformedText(
                annotatedString,
                OffsetMapping.Identity
            )
        }
    }

    private fun updateCurrentSpanStyle() {
        this.currentStyles.clear()

        val currentStyles = if (selection.collapsed) {
            getRichSpanByTextIndex(textIndex = selection.min - 1)
        } else {
            getRichSpanListByTextRange(selection).distinct()
        }

        this.currentStyles.addAll(currentStyles)
    }

    private fun getRichSpanByTextIndex(textIndex: Int): List<SpanStyle> {
        return spans.filter { textIndex >= it.from && textIndex <= it.to }
            .map { it.style }
    }

    private fun getRichSpanListByTextRange(selection: TextRange): List<SpanStyle> {
        val matchingSpans = mutableListOf<SpanStyle>()

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

    fun toggleStyle(style: SpanStyle) {
        if (currentStyles.contains(style)) {
            removeStyle(style)
        } else {
            addStyle(style)
        }
    }

    fun updateStyle(style: SpanStyle) {
        currentStyles.clear()
        currentStyles.add(style)

        if (!selection.collapsed) {
            applyStylesToSelectedText(style)
        }
    }

    private fun addStyle(style: SpanStyle) {
        if (!currentStyles.contains(style)) {
            currentStyles.add(style)
        }

        if (!selection.collapsed) {
            applyStylesToSelectedText(style)
        }
    }

    private fun applyStylesToSelectedText(style: SpanStyle) {
        if (textFieldValue.selection.collapsed) {
            return
        }

        val fromIndex = textFieldValue.selection.min
        val toIndex = textFieldValue.selection.max

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
        updateTextFieldValue()
    }

    private fun mergeSequentialParts() {
        if (spans.isEmpty()) return

        val result = mutableListOf<RichTextSpan>()
        val styleToMergedSegments = mutableMapOf<SpanStyle, RichTextSpan>()

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

    private fun removeStyle(style: SpanStyle) {
        if (currentStyles.contains(style)) {
            currentStyles.remove(style)
        }

        if (!selection.collapsed)
            removeStylesFromSelectedText(style)
    }

    private fun removeStylesFromSelectedText(style: SpanStyle) {
        if (textFieldValue.selection.collapsed) {
            return
        }

        val fromIndex = textFieldValue.selection.min
        val toIndex = textFieldValue.selection.max

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
        updateTextFieldValue()
    }

    fun onTextFieldValueChange(newTextFieldValue: TextFieldValue) {
        if (newTextFieldValue.text.length > textFieldValue.text.length)
            handleAddingCharacters(newTextFieldValue)
        else if (newTextFieldValue.text.length < textFieldValue.text.length)
            handleRemovingCharacters(newTextFieldValue)
        else if (
            newTextFieldValue.text == textFieldValue.text &&
            newTextFieldValue.selection != textFieldValue.selection
        ) {
            adjustSelection(newTextFieldValue.selection)
            return
        }

        collapseParts(textLastIndex = newTextFieldValue.text.lastIndex)
        updateTextFieldValue(newTextFieldValue)
    }

    private fun adjustSelection(selection: TextRange? = null) {
        if (selection != null) {
            updateTextFieldValue(textFieldValue.copy(selection = selection))
        }
    }

    private fun handleAddingCharacters(newValue: TextFieldValue) {
        val typedChars = newValue.text.length - textFieldValue.text.length
        val startTypeIndex = newValue.selection.min - typedChars

        moveSpans(startTypeIndex, typedChars)

        val selectedStyles = currentStyles.toMutableList()
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
        selectedStyles: MutableList<SpanStyle>,
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

    private fun handleRemovingCharacters(
        newTextFieldValue: TextFieldValue
    ) {
        val removedChars = textFieldValue.text.length - newTextFieldValue.text.length
        val startRemoveIndex = newTextFieldValue.selection.min + removedChars
        val endRemoveIndex = newTextFieldValue.selection.min
        val removeRange = endRemoveIndex until startRemoveIndex

        val iterator = spans.iterator()

        val partsCopy = spans.toMutableList()

        while (iterator.hasNext()) {
            val part = iterator.next()
            val index = partsCopy.indexOf(part)

            if (removeRange.last < part.from) {
                partsCopy[index] = part.copy(
                    from = part.from - removedChars,
                    to = part.to - removedChars
                )
            } else if (removeRange.first <= part.from && removeRange.last >= part.to) {
                // Remove the element from the copy.
                partsCopy.removeAt(index)
            } else if (removeRange.first <= part.from) {
                partsCopy[index] = part.copy(
                    from = max(0, removeRange.first),
                    to = min(newTextFieldValue.text.length, part.to - removedChars)
                )
            } else if (removeRange.last <= part.to) {
                partsCopy[index] = part.copy(to = part.to - removedChars)
            } else if (removeRange.first < part.to) {
                partsCopy[index] = part.copy(to = removeRange.first)
            }
        }

        spans.clear()
        spans.addAll(partsCopy)
    }

    private fun updateSelectedTextParts(
        update: (part: RichTextSpan) -> RichTextSpan
    ) {
        if (textFieldValue.selection.collapsed) {
            return
        }

        val fromIndex = textFieldValue.selection.min
        val toIndex = textFieldValue.selection.max

        val selectedParts = spans.filter { part ->
            part.from < toIndex && part.to >= fromIndex
        }

//        if(selectedParts.isEmpty()){
//            currentStyles.first {
//
//            }
//        }
        selectedParts.forEach { part ->
            val index = spans.indexOf(part)
            if (index !in spans.indices) return@forEach

            if (part.from < fromIndex && part.to >= toIndex) {
                spans[index] = part.copy(to = fromIndex - 1)
                spans.add(
                    index + 1,
                    update(
                        part.copy(
                            from = fromIndex,
                            to = toIndex - 1
                        )
                    )
                )
                spans.add(index + 2, part.copy(from = toIndex))
            } else if (part.from < fromIndex) {
                spans[index] = part.copy(
                    to = fromIndex - 1
                )
                spans.add(index + 1, update(part.copy(from = fromIndex)))
            } else if (part.to >= toIndex) {
                spans[index] = update(part.copy(to = toIndex - 1))
                spans.add(index + 1, part.copy(from = toIndex))
            } else {
                spans[index] = update(part)
            }
        }
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

    internal fun merge(nextItem: RichTextState) {
        val text = this.text + "\n" + nextItem.text
        val existingParts = ArrayList(this.spans)
        this.spans.addAll(nextItem.spans)
        forwardParts(existingParts.size, this.spans.size, this.text.length + 1)
        val newTextField = TextFieldValue(text, selection = TextRange(text.length))
        updateTextFieldValue(newTextField)
    }

    fun hasStyle(style: SpanStyle) = currentStyles.contains(style)

    companion object {
        internal val DefaultSpanStyle = SpanStyle()
    }
}

