package com.canopas.editor.ui.data

import android.text.Editable
import android.text.Spannable
import android.text.style.BulletSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.TextRange
import com.canopas.editor.ui.model.ListType
import com.canopas.editor.ui.model.QuillSpan
import com.canopas.editor.ui.model.QuillTextSpan
import com.canopas.editor.ui.utils.TextSpanStyle
import kotlin.math.max
import kotlin.math.min

class QuillTextManager(quillSpan: QuillSpan) {

    private var editable: Editable =
        Editable.Factory()
            .newEditable(quillSpan.spans.joinToString(separator = "") { it.insert ?: "" })
    internal val quillTextSpans: MutableList<QuillTextSpan> = mutableListOf()

    init {
        quillSpan.spans.forEachIndexed { index, span ->
            val attributes = span.attributes
            val startIndex = if (index == 0) 0 else quillTextSpans.last().to + 1
            val fromIndex = editableText.indexOf(span.insert ?: "", startIndex = startIndex)
            val endIndex = fromIndex + (span.insert?.length ?: 0) - 1

            val textSpanStyles = attributes?.let { attrs ->
                mutableListOf<TextSpanStyle>().apply {
                    attrs.header?.let { header ->
                        TextSpanStyle.HeaderMap.headerMap["$header"]?.let { add(it) }
                    }
                    if (attrs.bold == true) add(TextSpanStyle.BoldStyle)
                    if (attrs.italic == true) add(TextSpanStyle.ItalicStyle)
                    if (attrs.underline == true) add(TextSpanStyle.UnderlineStyle)
                    if (attrs.list == ListType.bullet) add(TextSpanStyle.BulletStyle)
                }
            } ?: mutableListOf(TextSpanStyle.Default)

            quillTextSpans.add(
                QuillTextSpan(
                    from = fromIndex,
                    to = endIndex,
                    style = textSpanStyles
                )
            )
        }
    }

    internal val editableText: String
        get() = editable.toString()

    private var selection = TextRange(0, 0)
    internal val currentStyles = mutableStateListOf<TextSpanStyle>()
    private var rawText: String = editableText

    internal fun setEditable(editable: Editable) {
        editable.append(editableText)
        this.editable = editable
        if (editableText.isNotEmpty()) updateText()
    }

    private fun updateText() {
        editable.removeSpans<RelativeSizeSpan>()
        editable.removeSpans<StyleSpan>()
        editable.removeSpans<UnderlineSpan>()
        editable.removeSpans<BulletSpan>()

        try {
            quillTextSpans.forEach {
                it.style.forEach { style ->
                    editable.setSpan(
                        style.style,
                        it.from,
                        it.to + 1,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateCurrentSpanStyle()
    }

    private fun updateCurrentSpanStyle() {
        if (selection.collapsed && selection.min == 0) return
        currentStyles.clear()

        val currentStyles = if (selection.collapsed) {
            getRichSpanByTextIndex(textIndex = selection.min - 1)
        } else {
            getRichSpanListByTextRange(selection).distinct()
        }

        val currentSpan = quillTextSpans.findLast {
            it.from <= selection.min - 2 && it.to >= selection.min - 2 && it.style.contains(
                TextSpanStyle.BulletStyle
            )
        }

        if (currentSpan != null && this.selection.collapsed) {
            if (editable[selection.min - 1] == '\n' && editable[selection.min - 2] == '\n') {
                removeStyle(TextSpanStyle.BulletStyle)
            } else {
                this.currentStyles.addAll(currentStyles)
            }
        } else {
            this.currentStyles.addAll(currentStyles)
        }
    }

    private fun getRichSpanByTextIndex(textIndex: Int): List<TextSpanStyle> {
        return quillTextSpans
            .filter { textIndex >= it.from && textIndex <= it.to }
            .map { it.style }
            .flatten()
    }

    private fun getRichSpanListByTextRange(selection: TextRange): List<TextSpanStyle> {
        val matchingSpans = mutableListOf<TextSpanStyle>()
        val currentSpan = quillTextSpans.find { it.from <= selection.min && it.to >= selection.min }
        if (currentSpan != null) {
            matchingSpans.addAll(currentSpan.style)
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
            if (style == TextSpanStyle.BulletStyle && editable[selection.min - 1] == '\n') {
                editable.delete(selection.min - 1, selection.min)
            } else if (style == TextSpanStyle.BulletStyle && editable[selection.min - 1] != '\n') {
                return
            }
            currentStyles.remove(style)
        }

        if (!selection.collapsed) {
            val fromIndex = selection.min
            val toIndex = selection.max - 1

            quillTextSpans.firstOrNull { it.from <= fromIndex && it.to >= toIndex }
                ?.let { selectedSpan ->
                    val index = quillTextSpans.indexOf(selectedSpan)
                    val updatedStyle = selectedSpan.style.filterNot { it == style }
                    val newSpans = mutableListOf<QuillTextSpan>()

                    when {
                        fromIndex == selectedSpan.from && toIndex == selectedSpan.to -> {
                            quillTextSpans[index] = selectedSpan.copy(style = updatedStyle)
                        }

                        fromIndex == selectedSpan.from && toIndex < selectedSpan.to -> {
                            newSpans.add(QuillTextSpan(fromIndex, toIndex, updatedStyle))
                            newSpans.add(
                                QuillTextSpan(
                                    toIndex + 1,
                                    selectedSpan.to,
                                    selectedSpan.style
                                )
                            )
                            quillTextSpans.removeAt(index)
                            quillTextSpans.addAll(index, newSpans)
                        }

                        fromIndex > selectedSpan.from -> {
                            newSpans.add(
                                QuillTextSpan(
                                    selectedSpan.from,
                                    fromIndex - 1,
                                    selectedSpan.style
                                )
                            )
                            newSpans.add(QuillTextSpan(fromIndex, toIndex, updatedStyle))
                            newSpans.add(
                                QuillTextSpan(
                                    toIndex + 1,
                                    selectedSpan.to,
                                    selectedSpan.style
                                )
                            )
                            quillTextSpans.removeAt(index)
                            quillTextSpans.addAll(index, newSpans)
                        }

                        else -> {}
                    }
                }
            updateText()
        }
    }

    private fun addStyle(style: TextSpanStyle) {
        when {
            selection.min > 0 -> {
                if (style != TextSpanStyle.BulletStyle || editable[selection.min - 1] == '\n') {
                    currentStyles.add(style)
                    if (style == TextSpanStyle.BulletStyle && selection.collapsed) {
                        editable.insert(selection.min, "\u200B")
                    }
                }
            }

            selection.min == 0 && style == TextSpanStyle.BulletStyle && selection.collapsed -> {
                currentStyles.add(style)
                editable.insert(selection.min, "\u200B")
            }

            !currentStyles.contains(style) -> currentStyles.add(style)
        }

        if ((style.isHeaderStyle() || style.isDefault()) && selection.collapsed) {
            handleAddHeaderStyle(style)
        }

        if (!selection.collapsed || selection.min <= 0 ||
            (style != TextSpanStyle.BulletStyle || editable[selection.min - 1] == '\n')
        ) {
            applyStylesToSelectedText(style)
        }
    }

    private fun handleAddHeaderStyle(style: TextSpanStyle, text: String = rawText) {
        if (text.isEmpty()) return
        val fromIndex = selection.min
        val toIndex = if (selection.collapsed) fromIndex else selection.max

        val currentSpan = quillTextSpans.find { it.from <= fromIndex && it.to >= toIndex }
        val index = quillTextSpans.indexOf(currentSpan)
        quillTextSpans[index] =
            currentSpan?.copy(
                style = currentSpan.style.filterNot { it.isHeaderStyle() } + listOf(style)
            ) ?: return
        updateText()
    }

    private fun handleRemoveHeaderStyle(text: String = rawText) {
        if (text.isEmpty()) return

        val fromIndex = selection.min
        val toIndex = selection.max

        val startIndex: Int = max(0, text.lastIndexOf("\n", fromIndex - 1))
        var endIndex: Int = text.indexOf("\n", toIndex)
        if (endIndex == -1) endIndex = text.length - 1

        val nextNewlineIndex = text.lastIndexOf("\n", startIndex)

        if (quillTextSpans.none { it.from < nextNewlineIndex && it.to >= startIndex && it.style.any { it.isHeaderStyle() } }) return

        quillTextSpans.removeAll {
            it.from < endIndex && it.to >= startIndex && it.style.size == 1 && it.style.first()
                .isHeaderStyle()
        }
    }

    private fun applyStylesToSelectedText(style: TextSpanStyle) {
        if (selection.collapsed) return

        val fromIndex = selection.min
        val toIndex = selection.max

        val selectedSpan = quillTextSpans.find { it.from <= fromIndex && (it.to + 1) >= toIndex }
        val index = quillTextSpans.indexOf(selectedSpan)

        when {
            selectedSpan != null -> {
                if (fromIndex == selectedSpan.from && toIndex < selectedSpan.to) {
                    val newSpan = QuillTextSpan(
                        from = fromIndex,
                        to = toIndex - 1,
                        style = selectedSpan.style + listOf(style)
                    )
                    val nextSpan = QuillTextSpan(
                        from = toIndex,
                        to = selectedSpan.to,
                        style = selectedSpan.style
                    )
                    quillTextSpans[index] = newSpan
                    quillTextSpans.add(index + 1, nextSpan)
                } else if (fromIndex > selectedSpan.from && toIndex < selectedSpan.to) {
                    val previousSpan = QuillTextSpan(
                        from = selectedSpan.from,
                        to = fromIndex - 1,
                        style = selectedSpan.style
                    )
                    val newSpan = QuillTextSpan(
                        from = fromIndex,
                        to = toIndex - 1,
                        style = selectedSpan.style + listOf(style)
                    )
                    val nextSpan = QuillTextSpan(
                        from = toIndex,
                        to = selectedSpan.to,
                        style = selectedSpan.style
                    )
                    quillTextSpans[index] = previousSpan
                    quillTextSpans.add(index + 1, newSpan)
                    quillTextSpans.add(index + 2, nextSpan)
                } else if (fromIndex > selectedSpan.from && (toIndex == selectedSpan.to || toIndex == (selectedSpan.to + 1))) {
                    val previousSpan = QuillTextSpan(
                        from = selectedSpan.from,
                        to = fromIndex - 1,
                        style = selectedSpan.style
                    )
                    val newSpan = QuillTextSpan(
                        from = fromIndex,
                        to = toIndex - 1,
                        style = selectedSpan.style + listOf(style)
                    )
                    quillTextSpans[index] = previousSpan
                    quillTextSpans.add(index + 1, newSpan)
                } else {
                    quillTextSpans[index] =
                        selectedSpan.copy(style = selectedSpan.style + listOf(style))
                }
            }

            else -> quillTextSpans.add(
                QuillTextSpan(
                    from = fromIndex,
                    to = toIndex - 1,
                    style = listOf(style)
                )
            )
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
        if (newText.length > rawText.length) handleAddingCharacters(newText)
        else if (newText.length < rawText.length) handleRemovingCharacters(newText)

        updateText()
        this.rawText = newText.toString()
    }

    internal fun handleAddingCharacters(newValue: Editable) {
        val typedCharsCount = newValue.length - rawText.length
        val startTypeIndex = selection.min - typedCharsCount

        if (newValue.getOrNull(startTypeIndex) == '\n' && currentStyles.any { it.isHeaderStyle() }) {
            currentStyles.clear()
            quillTextSpans.find { it.from <= startTypeIndex && it.to >= startTypeIndex }
                ?.let { span ->
                    val index = quillTextSpans.indexOf(span)
                    val styles = span.style.filterNot { it.isHeaderStyle() }
                    val updatedSpan = span.copy(style = styles)
                    quillTextSpans[index] = updatedSpan
                }
        }

        val selectedStyles = currentStyles.distinct()
        moveSpans(startTypeIndex, typedCharsCount)

        val currentSpan =
            quillTextSpans.find { it.from <= startTypeIndex && it.to >= startTypeIndex }
        val isBulletStyle = selectedStyles.contains(TextSpanStyle.BulletStyle)

        currentSpan?.let { span ->
            val index = quillTextSpans.indexOf(span)
            val styles = (span.style + selectedStyles).distinct()
            val from = span.from
            val to = span.to

            when {
                span.style == selectedStyles -> {
                    if (isBulletStyle && newValue.getOrNull(startTypeIndex) == '\n') {
                        if (newValue.getOrNull(startTypeIndex - 1) != '\n' && startTypeIndex == to) {
                            quillTextSpans.add(
                                index + 1,
                                span.copy(
                                    from = startTypeIndex,
                                    to = startTypeIndex + typedCharsCount - 1,
                                    style = selectedStyles
                                )
                            )
                            quillTextSpans.add(
                                index + 2,
                                span.copy(
                                    from = startTypeIndex + typedCharsCount,
                                    to = to + typedCharsCount,
                                    style = selectedStyles
                                )
                            )
                        } else {
                            if (startTypeIndex in (from + 1) until to) {
                                val newSpans = mutableListOf<QuillTextSpan>()
                                newSpans.add(span.copy(to = startTypeIndex - 1, style = styles))
                                newSpans.add(
                                    span.copy(
                                        from = startTypeIndex,
                                        to = startTypeIndex + typedCharsCount - 1,
                                        style = selectedStyles
                                    )
                                )
                                newSpans.add(
                                    span.copy(
                                        from = startTypeIndex + typedCharsCount,
                                        to = to + typedCharsCount,
                                        style = styles
                                    )
                                )
                                quillTextSpans.removeAt(index)
                                quillTextSpans.addAll(index, newSpans)
                            } else {
                                val updatedSpan = span.copy(to = to + typedCharsCount, style = selectedStyles)
                                quillTextSpans[index] = updatedSpan
                                quillTextSpans.add(index + 1, updatedSpan)
                            }
                        }
                    } else {
                        quillTextSpans[index] = span.copy(to = to + typedCharsCount, style = styles)
                    }
                }

                span.style != selectedStyles -> {
                    quillTextSpans.removeAt(index)
                    val newSpans = mutableListOf<QuillTextSpan>()
                    if (startTypeIndex != from) {
                        newSpans.add(span.copy(to = startTypeIndex - 1))
                    }
                    newSpans.add(
                        span.copy(
                            from = startTypeIndex,
                            to = startTypeIndex + typedCharsCount - 1,
                            style = selectedStyles
                        )
                    )
                    newSpans.add(
                        span.copy(
                            from = startTypeIndex + typedCharsCount,
                            to = to + typedCharsCount,
                            style = styles
                        )
                    )
                    quillTextSpans.addAll(index, newSpans)
                }

                startTypeIndex == from && to == startTypeIndex -> {
                    quillTextSpans[index] =
                        span.copy(to = to + typedCharsCount, style = selectedStyles)
                }

                startTypeIndex == from && to > startTypeIndex -> {
                    quillTextSpans[index] =
                        span.copy(to = startTypeIndex + typedCharsCount - 1, style = selectedStyles)
                    quillTextSpans.add(
                        index + 1,
                        span.copy(
                            from = startTypeIndex + typedCharsCount,
                            to = to + typedCharsCount,
                            style = styles
                        )
                    )
                }

                startTypeIndex > from && to == startTypeIndex -> {
                    quillTextSpans[index] = span.copy(to = to + typedCharsCount, style = styles)
                }

                startTypeIndex in (from + 1) until to -> {
                    val newSpans = mutableListOf<QuillTextSpan>()
                    newSpans.add(span.copy(to = startTypeIndex - 1, style = styles))
                    newSpans.add(
                        span.copy(
                            from = startTypeIndex,
                            to = startTypeIndex + typedCharsCount - 1,
                            style = selectedStyles
                        )
                    )
                    newSpans.add(
                        span.copy(
                            from = startTypeIndex + typedCharsCount,
                            to = to + typedCharsCount,
                            style = styles
                        )
                    )
                    quillTextSpans.removeAt(index)
                    quillTextSpans.addAll(index, newSpans)
                }

                else -> {}
            }
        } ?: quillTextSpans.add(
            QuillTextSpan(
                from = startTypeIndex,
                to = startTypeIndex + typedCharsCount - 1,
                style = selectedStyles
            )
        )
    }

    private fun moveSpans(startTypeIndex: Int, by: Int) {
        val filteredSpans = quillTextSpans.filter { it.from > startTypeIndex }

        filteredSpans.forEach {
            val index = quillTextSpans.indexOf(it)
            quillTextSpans[index] =
                it.copy(
                    from = it.from + by,
                    to = it.to + by,
                )
        }
    }

    private fun handleRemovingCharacters(newText: Editable) {
        if (newText.isEmpty()) {
            quillTextSpans.clear()
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

        val iterator = quillTextSpans.iterator()

        val partsCopy = quillTextSpans.toMutableList()

        while (iterator.hasNext()) {
            val part = iterator.next()
            val index = partsCopy.indexOf(part)
            val previousPart = partsCopy.getOrNull(index - 1)
            val nextPart = partsCopy.getOrNull(index + 1)

            if (removeRange.last < part.from) {
                if (part.style.contains(TextSpanStyle.BulletStyle)) {
                    if (
                        previousPart?.style?.contains(TextSpanStyle.BulletStyle) == true ||
                        nextPart?.style?.contains(TextSpanStyle.BulletStyle) == true
                    ) {
                        partsCopy[index] =
                            part.copy(
                                from = part.from - removedCharsCount,
                                to = part.to - removedCharsCount
                            )
                    } else {
                        partsCopy[index] =
                            part.copy(
                                from = part.from - removedCharsCount,
                                to = part.to - removedCharsCount,
                                style = part.style.filterNot { it == TextSpanStyle.BulletStyle }
                            )
                    }
                } else {
                    partsCopy[index] =
                        part.copy(
                            from = part.from - removedCharsCount,
                            to = part.to - removedCharsCount
                        )
                }
            } else if (removeRange.first <= part.from && removeRange.last >= part.to) {
                partsCopy.removeAt(index)
            } else if (removeRange.first <= part.from) {
                partsCopy[index] =
                    part.copy(
                        from = max(0, removeRange.first),
                        to = min(newText.length, part.to - removedCharsCount)
                    )
            } else if (removeRange.last <= part.to) {
                partsCopy[index] = part.copy(to = part.to - removedCharsCount)
            } else if (removeRange.first < part.to) {
                partsCopy[index] = part.copy(to = removeRange.first)
            }
        }

        quillTextSpans.clear()
        quillTextSpans.addAll(partsCopy)
    }

    internal fun adjustSelection(selection: TextRange) {
        if (this.selection != selection) {
            this.selection = selection
            updateCurrentSpanStyle()
        }
    }

    fun hasStyle(style: TextSpanStyle) = currentStyles.contains(style)

    fun reset() {
        quillTextSpans.clear()
        this.rawText = ""
        this.editable.clear()
        updateText()
    }

    companion object {
        fun TextSpanStyle.isDefault(): Boolean {
            return this == TextSpanStyle.Default
        }

        fun TextSpanStyle.isHeaderStyle(): Boolean {
            val headers =
                listOf(
                    TextSpanStyle.H1Style,
                    TextSpanStyle.H2Style,
                    TextSpanStyle.H3Style,
                    TextSpanStyle.H4Style,
                    TextSpanStyle.H5Style,
                    TextSpanStyle.H6Style,
                )

            return headers.contains(this)
        }

        internal fun TextSpanStyle.headerLevel(): Int? {
            return when (this) {
                TextSpanStyle.H1Style -> 1
                TextSpanStyle.H2Style -> 2
                TextSpanStyle.H3Style -> 3
                TextSpanStyle.H4Style -> 4
                TextSpanStyle.H5Style -> 5
                TextSpanStyle.H6Style -> 6
                else -> null
            }
        }

        internal inline fun <reified T> Editable.removeSpans() {
            val allSpans = getSpans(0, length, T::class.java)
            for (span in allSpans) {
                removeSpan(span)
            }
        }
    }
}
