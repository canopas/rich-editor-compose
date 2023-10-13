package com.canopas.editor.ui.data

import android.util.Log
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
import com.canopas.editor.ui.parser.json.toSpansString
import kotlin.math.max
import kotlin.math.min

class RichTextState internal constructor(
    val richText: String = "",
    val spans: MutableList<RichTextPart> = mutableListOf()
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
                part.spanStyle.let {
                    addStyle(
                        style = it,
                        start = part.fromIndex,
                        end = part.toIndex + 1,
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
        return spans.filter { textIndex >= it.fromIndex && textIndex <= it.toIndex }
            .mapNotNull { it.spanStyle }
    }

    private fun getRichSpanListByTextRange(selection: TextRange): List<SpanStyle> {
        val matchingSpans = mutableListOf<SpanStyle>()

        for (part in spans) {
            val partRange = TextRange(part.fromIndex, part.toIndex)
            if (selection.overlaps(partRange)) {
                part.spanStyle.let {
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
        updateSelectedTextParts { part ->
            part.copy(spanStyle = style)
        }
        updateTextFieldValue()
    }

    private fun removeStyle(style: SpanStyle) {
        if (currentStyles.contains(style)) {
            currentStyles.remove(style)
        }

        if (!selection.collapsed)
            removeStylesFromSelectedText(style)
    }

    private fun removeStylesFromSelectedText(style: SpanStyle) {
        updateSelectedTextParts { part ->
            part.copy(spanStyle = style)
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
        //  Log.d("XXX", "after collapseParts span size ${spans.size}")

    }

    private fun adjustSelection(selection: TextRange? = null) {
        if (selection != null) {
            updateTextFieldValue(textFieldValue.copy(selection = selection))
        }
    }

//    private fun handleAddingCharacters2(newValue: TextFieldValue) {
//        val typedChars = newValue.text.length - textFieldValue.text.length
//        val startTypeIndex = newValue.selection.min - typedChars
//
//        val currentStyles = this.currentStyles.toMutableStateList()
//
//        val startRichTextPartIndex = spans.indexOfFirst {
//            (startTypeIndex - 1) in it.fromIndex..it.toIndex
//        }
//        val endRichTextPartIndex = spans.indexOfFirst {
//            startTypeIndex in it.fromIndex..it.toIndex
//        }
//
//        val startRichTextPart = spans.getOrNull(startRichTextPartIndex)
//        val endRichTextPart = spans.getOrNull(endRichTextPartIndex)
//
//        if (currentStyles == startRichTextPart?.spanStyle) {
//            spans[startRichTextPartIndex] = startRichTextPart.copy(
//                toIndex = startRichTextPart.toIndex + typedChars
//            )
//

//            if (startRichTextPartIndex < spans.lastIndex) {
//                forwardParts(
//                    fromIndex = startRichTextPartIndex + 1,
//                    toIndex = spans.lastIndex,
//                    by = typedChars,
//                )
//            }
//        } else if (currentStyles == endRichTextPart?.spanStyle) {
//            spans[endRichTextPartIndex] = endRichTextPart.copy(
//                toIndex = endRichTextPart.toIndex + typedChars
//            )
//
//            if (endRichTextPartIndex < spans.lastIndex) {
//                forwardParts(
//                    fromIndex = endRichTextPartIndex + 1,
//                    toIndex = spans.lastIndex,
//                    by = typedChars,
//                )
//            }
//        } else if (startRichTextPart == endRichTextPart && startRichTextPart != null) {
//            spans[startRichTextPartIndex] = startRichTextPart.copy(
//                toIndex = startTypeIndex - 1
//            )
//            spans.add(
//                startRichTextPartIndex + 1, startRichTextPart.copy(
//                    fromIndex = startTypeIndex + typedChars,
//                    toIndex = startRichTextPart.toIndex + typedChars
//                )
//            )
//            spans.add(
//                startRichTextPartIndex + 1, RichTextPart(
//                    fromIndex = startTypeIndex,
//                    toIndex = startTypeIndex + typedChars - 1,
//                    spanStyle = currentStyles
//                )
//            )
//
//            if ((startRichTextPartIndex + 2) < spans.lastIndex) {
//                forwardParts(
//                    fromIndex = startRichTextPartIndex + 3,
//                    toIndex = spans.lastIndex,
//                    by = typedChars,
//                )
//            }
//        } else if (endRichTextPart == null) {
//            spans.add(
//                RichTextPart(
//                    fromIndex = startTypeIndex,
//                    toIndex = startTypeIndex + typedChars - 1,
//                    spanStyle = currentStyles
//                )
//            )
//        } else {
//            spans.add(
//                startRichTextPartIndex + 1, RichTextPart(
//                    fromIndex = startTypeIndex,
//                    toIndex = startTypeIndex + typedChars - 1,
//                    spanStyle = currentStyles
//                )
//            )
//
//            if ((startRichTextPartIndex + 1) < spans.lastIndex) {
//                forwardParts(
//                    fromIndex = startRichTextPartIndex + 2,
//                    toIndex = spans.lastIndex,
//                    by = typedChars,
//                )
//            }
//        }
//    }


    private fun handleAddingCharacters(newValue: TextFieldValue) {
        val typedChars = newValue.text.length - textFieldValue.text.length
        val startTypeIndex = newValue.selection.min - typedChars

        logSpans("original")

        val selectedStyles = currentStyles.toMutableList()


//        if (currentStyles.isNotEmpty()) {
        // val tempSpans = spans.toList()

        val startParts = getAllActiveSpan(startTypeIndex - 1)
        val endParts = getAllActiveSpan(startTypeIndex)

        Log.d("XXX", "matching span with START ${startParts.size} END ${endParts.size}")
        Log.d("XXX", "current styles ${currentStyles.size}")

        val tempSpans = spans.toList()
        if (startParts.isNotEmpty() && endParts.isNotEmpty() && startParts == endParts) {
            Log.d("XXX", "found start and end matches")
            startParts.forEach {
                Log.d("XXX", "handle Span ${it.spanStyle.toSpansString()}")

                val spanIndex = tempSpans.indexOf(it)
                handleSpan(it, spanIndex, typedChars, startTypeIndex, selectedStyles)
            }

        } else if (startParts.isNotEmpty() && endParts.isNotEmpty() && startParts.size > endParts.size) {
            val matchingParts = startParts.filter { it in endParts }
            val distinctParts = startParts.filterNot { it in endParts }

            startParts.forEach {
                Log.d("XXX", "handle matching Start Span ${it.spanStyle.toSpansString()}")
                val spanIndex = tempSpans.indexOf(it)
                handleSpan(it, spanIndex, typedChars, startTypeIndex, selectedStyles)
            }

        } else if (startParts.isNotEmpty() && endParts.isNotEmpty() && startParts.size < endParts.size) {
            val matchingParts = endParts.filter { it in startParts }
            val distinctParts = endParts.filterNot { it in startParts }
            matchingParts.forEach {
                Log.d("XXX", "handle matching End Span ${it.spanStyle.toSpansString()}")
                val spanIndex = tempSpans.indexOf(it)
                handleSpan(it, spanIndex, typedChars, startTypeIndex, selectedStyles)
            }

            distinctParts.forEach {
                Log.d("XXX", "handle distinctParts End Span ${it.spanStyle.toSpansString()}")
                val spanIndex = spans.indexOf(it)
                //   handleSpan(it, spanIndex, typedChars, startTypeIndex, selectedStyles)
                if (currentStyles.contains(it.spanStyle)) {
                    spans[spanIndex] = it.copy(
                        toIndex = it.toIndex + typedChars
                    )
                    selectedStyles.remove(it.spanStyle)
                } else {
                    forwardSpans(startTypeIndex, typedChars)
                }
            }
        } else if (startParts.isNotEmpty()) {

            if (currentStyles.isEmpty()) {
                Log.d("XXX", "No style keep forward ")
                forwardSpans(startTypeIndex, typedChars)
            } else {
                startParts.forEach { richTextPart ->
                    Log.d("XXX", "handle START Span ${richTextPart.spanStyle.toSpansString()}")
                    val spanIndex = tempSpans.indexOf(richTextPart)
                    handleSpan(richTextPart, spanIndex, typedChars, startTypeIndex, selectedStyles)
                }
            }
        } else if (endParts.isNotEmpty()) {
            if (currentStyles.isEmpty()) {
                Log.d("XXX", "No style keep forward ")
                forwardSpans(startTypeIndex, typedChars)
            } else {
                endParts.forEach { richTextPart ->
                    Log.d("XXX", "handle END Span ${richTextPart.spanStyle.toSpansString()}")
                    val spanIndex = tempSpans.indexOf(richTextPart)
                    handleSpan(richTextPart, spanIndex, typedChars, startTypeIndex, selectedStyles)
                }
            }
        } else {
            Log.d("XXX", "keep forwarding")
            forwardSpans(startTypeIndex + 1, typedChars)
        }

        selectedStyles.forEach {
            Log.d("XXX", "add style ${it.toSpansString()}")
            spans.add(
                RichTextPart(
                    fromIndex = startTypeIndex,
                    toIndex = startTypeIndex + typedChars - 1,
                    spanStyle = it
                )
            )
        }
        logSpans("final")

    }

    private fun handleSpan(
        richTextPart: RichTextPart,
        spanIndex: Int,
        typedChars: Int,
        startTypeIndex: Int,
        selectedStyles: MutableList<SpanStyle>
    ) {
        //    val spanIndex = spans.indexOf(richTextPart)
        if (currentStyles.contains(richTextPart.spanStyle)) {
            spans[spanIndex] =
                richTextPart.copy(toIndex = richTextPart.toIndex + typedChars)
            if (spanIndex < spans.lastIndex) {
                forwardParts(
                    fromIndex = spanIndex + 1,
                    toIndex = spans.lastIndex,
                    by = typedChars,
                )
            }
            selectedStyles.remove(richTextPart.spanStyle)

        } else {
            Log.d(
                "XXX",
                "No style found for ${richTextPart.spanStyle.toSpansString()} selected style ${selectedStyles.size}"
            )
            logSpans("span now")
            spans[spanIndex] = richTextPart.copy(toIndex = startTypeIndex - 1)
            selectedStyles.remove(richTextPart.spanStyle)
            spans.add(
                index = spanIndex + 1, richTextPart.copy(
                    fromIndex = startTypeIndex + typedChars,
                    toIndex = richTextPart.toIndex + typedChars
                )
            )
//            selectedStyles.forEach {
//                spans.add(
//                    spanIndex + 1, RichTextPart(
//                        fromIndex = startTypeIndex,
//                        toIndex = startTypeIndex + typedChars - 1,
//                        spanStyle = it
//                    )
//                )
//            }
            //handle spans [{from:0, to:2, style: bold}, {from:4, to:5, style: bold}, {from:3, to:4, style: italic}]}
//            logSpans("added new  ")
//            Log.d("XXX", "spanIndex $spanIndex spans ${spans.lastIndex}")
//            if (spanIndex +  1 < spans.lastIndex) {
//                forwardParts(
//                    fromIndex = spanIndex + 2,
//                    toIndex = spans.lastIndex,
//                    by = typedChars,
//                )
//            }
            forwardSpans(startTypeIndex + 2, typedChars)
            //  selectedStyles.clear()

        }

        logSpans("handle")
        //  removeDuplicateSpans()
        // logSpans("removed DuplicateSpans")

    }

    //final spans [{from:0, to:2, style: bold}, {from:3, to:3, style: italic}, {from:4, to:6, style: bold}, {from:4, to:5, style: italic}]}
    fun logSpans(message: String) {
        Log.d(
            "XXX",
            "$message spans ${spans.map { "{from:${it.fromIndex}, to:${it.toIndex}, style: ${it.spanStyle.toSpansString()}}" }}}"
        )
    }

    private fun removeDuplicateSpans() {
        val mergedSpans = mutableListOf<RichTextPart>()

        val sortedSpans = spans.sortedBy { it.fromIndex }

        if (sortedSpans.isNotEmpty()) {
            var currentSpan = sortedSpans[0]

            for (i in 1 until sortedSpans.size) {
                val nextSpan = sortedSpans[i]

                if (currentSpan.spanStyle == nextSpan.spanStyle) {
                    if (currentSpan.toIndex >= nextSpan.fromIndex) {
                        currentSpan = RichTextPart(
                            currentSpan.fromIndex,
                            maxOf(currentSpan.toIndex, nextSpan.toIndex),
                            currentSpan.spanStyle
                        )
                    } else {
                        mergedSpans.add(currentSpan)
                        currentSpan = nextSpan
                    }
                } else {
                    mergedSpans.add(currentSpan)
                    currentSpan = nextSpan
                }
            }

            mergedSpans.add(currentSpan)
        }

        spans.clear()
        spans.addAll(mergedSpans)
    }

    private fun forwardSpans(startTypeIndex: Int, by: Int) {
        val filteredSpans = spans.filter { it.fromIndex >= startTypeIndex }

        filteredSpans.forEach {
            Log.d(
                "XXX",
                "startTypeIndex $startTypeIndex forward style ${it.spanStyle.toSpansString()}"
            )
            val index = spans.indexOf(it)
            spans[index] = it.copy(
                fromIndex = it.fromIndex + by,
                toIndex = it.toIndex + by,
            )
        }
    }

    private fun getAllActiveSpan(startIndex: Int): List<RichTextPart> {
        return spans.filter {
            startIndex in it.fromIndex..it.toIndex //&& currentStyles.contains(it.spanStyle)
        }
    }

    private fun getAllNotActiveSpan(startIndex: Int): List<RichTextPart> {
        return spans.filter {
            startIndex in it.fromIndex..it.toIndex && !currentStyles.contains(it.spanStyle)
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
            Log.d("XXX", "forwarding by $by style ${spans[index].spanStyle.toSpansString()}")
            spans[index] = spans[index].copy(
                fromIndex = spans[index].fromIndex + by,
                toIndex = spans[index].toIndex + by,
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

            if (removeRange.last < part.fromIndex) {
                partsCopy[index] = part.copy(
                    fromIndex = part.fromIndex - removedChars,
                    toIndex = part.toIndex - removedChars
                )
            } else if (removeRange.first <= part.fromIndex && removeRange.last >= part.toIndex) {
                // Remove the element from the copy.
                partsCopy.removeAt(index)
            } else if (removeRange.first <= part.fromIndex) {
                partsCopy[index] = part.copy(
                    fromIndex = max(0, removeRange.first),
                    toIndex = min(newTextFieldValue.text.length, part.toIndex - removedChars)
                )
            } else if (removeRange.last <= part.toIndex) {
                partsCopy[index] = part.copy(toIndex = part.toIndex - removedChars)
            } else if (removeRange.first < part.toIndex) {
                partsCopy[index] = part.copy(toIndex = removeRange.first)
            }
        }

        spans.clear()
        spans.addAll(partsCopy)
    }

    private fun updateSelectedTextParts(
        update: (part: RichTextPart) -> RichTextPart
    ) {
        if (textFieldValue.selection.collapsed) {
            return
        }

        val fromIndex = textFieldValue.selection.min
        val toIndex = textFieldValue.selection.max

        val selectedParts = spans.filter { part ->
            part.fromIndex < toIndex && part.toIndex >= fromIndex
        }

        selectedParts.forEach { part ->
            val index = spans.indexOf(part)
            if (index !in spans.indices) return@forEach

            if (part.fromIndex < fromIndex && part.toIndex >= toIndex) {
                spans[index] = part.copy(toIndex = fromIndex - 1)
                spans.add(
                    index + 1,
                    update(
                        part.copy(
                            fromIndex = fromIndex,
                            toIndex = toIndex - 1
                        )
                    )
                )
                spans.add(index + 2, part.copy(fromIndex = toIndex))
            } else if (part.fromIndex < fromIndex) {
                spans[index] = part.copy(
                    toIndex = fromIndex - 1
                )
                spans.add(index + 1, update(part.copy(fromIndex = fromIndex)))
            } else if (part.toIndex >= toIndex) {
                spans[index] = update(part.copy(toIndex = toIndex - 1))
                spans.add(index + 1, part.copy(fromIndex = toIndex))
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
            startRangeMap[part.fromIndex] = index
            endRangeMap[part.toIndex] = index
        }

        partsCopy.forEachIndexed { index, part ->
            if (removedIndexes.contains(index)) {
                return@forEachIndexed
            }

            val start = part.fromIndex
            val end = part.toIndex

            if (end < start) {
                removedIndexes.add(index)
                return@forEachIndexed
            }

            if (startRangeMap.containsKey(end + 1)) {
                val otherRangeIndex = requireNotNull(startRangeMap[end + 1])
                if (partsCopy[otherRangeIndex].spanStyle == part.spanStyle) {
                    partsCopy[index] = part.copy(
                        toIndex = partsCopy[otherRangeIndex].toIndex
                    )

                    // Remove collapsed values
                    startRangeMap.remove(end + 1)
                    endRangeMap.remove(end)
                    removedIndexes.add(otherRangeIndex)
                }
            }

            if (endRangeMap.containsKey(start - 1)) {
                val otherRangeIndex = requireNotNull(endRangeMap[start - 1])
                if (partsCopy[otherRangeIndex].spanStyle == part.spanStyle) {
                    partsCopy[index] = part.copy(
                        fromIndex = partsCopy[otherRangeIndex].fromIndex
                    )

                    // Remove collapsed values
                    startRangeMap.remove(start - 1)
                    endRangeMap.remove(start - 1)
                    removedIndexes.add(otherRangeIndex)
                }
            }

            partsCopy[index] = partsCopy[index].copy(
                fromIndex = max(0, partsCopy[index].fromIndex),
                toIndex = min(textLastIndex, partsCopy[index].toIndex),
            )
        }
        /*removedIndexes.reversed().forEach {
                    if (it in 0 until partsCopy.size) {
                        partsCopy.removeAt(it)
                    }
                }*/
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

