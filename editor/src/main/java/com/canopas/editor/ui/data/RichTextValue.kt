package com.canopas.editor.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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

data class RichTextValue constructor(
    internal var textFieldValue: TextFieldValue,
    internal val currentStyles: MutableList<RichTextAttribute> = mutableListOf(),
    internal val parts: MutableList<RichTextPart> = mutableListOf()
) {

    constructor(
        text: String = "",
        currentStyles: MutableSet<RichTextAttribute> = mutableSetOf(),
        parts: MutableList<RichTextPart> = mutableListOf()
    ) : this(
        textFieldValue = TextFieldValue(
            text = text,
            selection = TextRange(text.length)
        ),
        currentStyles.toMutableStateList(),
        parts.toMutableStateList()
    ) {
        updateAnnotatedString()
    }


    val text get() = textFieldValue.text

    internal var visualTransformation: VisualTransformation by mutableStateOf(VisualTransformation.None)

    private var annotatedString by mutableStateOf(AnnotatedString(text = ""))

    fun updateAnnotatedString(newValue: TextFieldValue = textFieldValue) {
        annotatedString = buildAnnotatedString {
            append(newValue.text)
            parts.forEach { part ->
                val spanStyle = part.styles.fold(SpanStyle()) { spanStyle, richTextStyle ->
                    richTextStyle.apply(spanStyle)
                }

                addStyle(
                    style = spanStyle,
                    start = part.fromIndex,
                    end = part.toIndex + 1,
                )
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


    fun toggleStyle(style: RichTextAttribute) {
        if (currentStyles.contains(style)) {
            removeStyle(style)
        } else {
            addStyle(style)
        }
    }

    private fun addStyle(vararg style: RichTextAttribute) {
        currentStyles.addAll(style)
        applyStylesToSelectedText(*style)
    }

    fun updateStyles(newStyles: Set<RichTextAttribute>) {
        currentStyles.clear()
        currentStyles.addAll(newStyles)

        applyStylesToSelectedText(*newStyles.toTypedArray())
    }

    private fun applyStylesToSelectedText(vararg style: RichTextAttribute) {
        updateSelectedTextParts { part ->
            part.styles.addAll(style.toSet())
            part
        }
        updateAnnotatedString()
    }

    private fun removeStyle(vararg style: RichTextAttribute) {
        currentStyles.removeAll(style.toSet())
        removeStylesFromSelectedText(*style)
    }

    private fun removeStylesFromSelectedText(vararg style: RichTextAttribute) {
        updateSelectedTextParts { part ->
            part.styles.removeAll(style.toSet())
            part
        }
        updateAnnotatedString()
    }

    fun updateTextFieldValue(newValue: TextFieldValue): RichTextValue {
        var newTextFieldValue = newValue
        if (newTextFieldValue.text.length > textFieldValue.text.length) {
            newTextFieldValue = handleAddingCharacters(newTextFieldValue)
        } else if (newTextFieldValue.text.length < textFieldValue.text.length) {
            handleRemovingCharacters(newTextFieldValue)
        }

        updateCurrentStyles(newTextFieldValue = newTextFieldValue)

        collapseParts(textLastIndex = newTextFieldValue.text.lastIndex)

        updateAnnotatedString(newValue)
        return this
    }

    private fun handleAddingCharacters(
        newValue: TextFieldValue,
    ): TextFieldValue {

        val typedChars = newValue.text.length - textFieldValue.text.length
        val startTypeIndex = newValue.selection.min - typedChars

        if (newValue.text.getOrNull(startTypeIndex) == '\n') {
            removeTitleStylesIfAny()
        }

        val currentStyles = currentStyles.toMutableStateList()

        val startRichTextPartIndex = parts.indexOfFirst {
            (startTypeIndex - 1) in it.fromIndex..it.toIndex
        }
        val endRichTextPartIndex = parts.indexOfFirst {
            startTypeIndex in it.fromIndex..it.toIndex
        }

        val startRichTextPart = parts.getOrNull(startRichTextPartIndex)
        val endRichTextPart = parts.getOrNull(endRichTextPartIndex)

        if (currentStyles == startRichTextPart?.styles) {
            parts[startRichTextPartIndex] = startRichTextPart.copy(
                toIndex = startRichTextPart.toIndex + typedChars
            )

            if (startRichTextPartIndex < parts.lastIndex) {
                forwardParts(
                    fromIndex = startRichTextPartIndex + 1,
                    toIndex = parts.lastIndex,
                    by = typedChars,
                )
            }
        } else if (currentStyles == endRichTextPart?.styles) {
            parts[endRichTextPartIndex] = endRichTextPart.copy(
                toIndex = endRichTextPart.toIndex + typedChars
            )

            if (endRichTextPartIndex < parts.lastIndex) {
                forwardParts(
                    fromIndex = endRichTextPartIndex + 1,
                    toIndex = parts.lastIndex,
                    by = typedChars,
                )
            }
        } else if (startRichTextPart == endRichTextPart && startRichTextPart != null) {
            parts[startRichTextPartIndex] = startRichTextPart.copy(
                toIndex = startTypeIndex - 1
            )
            parts.add(
                startRichTextPartIndex + 1, startRichTextPart.copy(
                    fromIndex = startTypeIndex + typedChars,
                    toIndex = startRichTextPart.toIndex + typedChars
                )
            )
            parts.add(
                startRichTextPartIndex + 1, RichTextPart(
                    fromIndex = startTypeIndex,
                    toIndex = startTypeIndex + typedChars - 1,
                    styles = currentStyles.toMutableStateList()
                )
            )

            if ((startRichTextPartIndex + 2) < parts.lastIndex) {
                forwardParts(
                    fromIndex = startRichTextPartIndex + 3,
                    toIndex = parts.lastIndex,
                    by = typedChars,
                )
            }
        } else if (endRichTextPart == null) {
            parts.add(
                RichTextPart(
                    fromIndex = startTypeIndex,
                    toIndex = startTypeIndex + typedChars - 1,
                    styles = currentStyles
                )
            )
        } else {
            parts.add(
                startRichTextPartIndex + 1, RichTextPart(
                    fromIndex = startTypeIndex,
                    toIndex = startTypeIndex + typedChars - 1,
                    styles = currentStyles
                )
            )

            if ((startRichTextPartIndex + 1) < parts.lastIndex) {
                forwardParts(
                    fromIndex = startRichTextPartIndex + 2,
                    toIndex = parts.lastIndex,
                    by = typedChars,
                )
            }
        }

        return newValue
    }

    private fun forwardParts(
        fromIndex: Int,
        toIndex: Int,
        by: Int
    ) {
        val start = max(fromIndex, 0)
        val end = min(toIndex, parts.lastIndex)
        (start..end).forEach { index ->
            parts[index] = parts[index].copy(
                fromIndex = parts[index].fromIndex + by,
                toIndex = parts[index].toIndex + by,
            )
        }
    }

    private fun removeTitleStylesIfAny() {
        val isHeader = currentStyles.any { it.scope == TextAttributeScope.HEADER }
        if (isHeader) clearStyles()
    }

    private fun clearStyles() {
        currentStyles.clear()
        removeAllStylesFromSelectedText()
    }

    private fun removeAllStylesFromSelectedText() {
        updateSelectedTextParts { part ->
            part.styles.clear()
            part
        }
    }

    private fun handleRemovingCharacters(
        newTextFieldValue: TextFieldValue
    ) {
        val removedChars = textFieldValue.text.length - newTextFieldValue.text.length
        val startRemoveIndex = newTextFieldValue.selection.min + removedChars
        val endRemoveIndex = newTextFieldValue.selection.min
        val removeRange = endRemoveIndex until startRemoveIndex

        val iterator = parts.iterator()

        val partsCopy = parts.toMutableList()

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

        parts.clear()
        parts.addAll(partsCopy)
    }

    private fun updateCurrentStyles(
        newTextFieldValue: TextFieldValue
    ) {
        val newStyles = parts
            .firstOrNull {
                if (newTextFieldValue.selection.min == 0 && it.fromIndex == 0) {
                    return@firstOrNull true
                }
                (newTextFieldValue.selection.min - 1) in (it.fromIndex..it.toIndex)
            }
            ?.styles
            ?: currentStyles

        setCurrentStyles(newStyles.toSet())
    }

    private fun setCurrentStyles(currentStyles: Set<RichTextAttribute>) {
        this.currentStyles.clear()
        this.currentStyles.addAll(currentStyles)
    }

    private fun updateSelectedTextParts(
        update: (part: RichTextPart) -> RichTextPart
    ) {
        if (textFieldValue.selection.collapsed) {
            return
        }

        val fromIndex = textFieldValue.selection.min
        val toIndex = textFieldValue.selection.max

        val selectedParts = parts.filter { part ->
            part.fromIndex < toIndex && part.toIndex >= fromIndex
        }

        selectedParts.forEach { part ->
            val index = parts.indexOf(part)
            if (index !in parts.indices) return@forEach

            if (part.fromIndex < fromIndex && part.toIndex >= toIndex) {
                parts[index] = part.copy(toIndex = fromIndex - 1)
                parts.add(
                    index + 1,
                    update(
                        part.copy(
                            fromIndex = fromIndex,
                            toIndex = toIndex - 1
                        )
                    )
                )
                parts.add(index + 2, part.copy(fromIndex = toIndex))
            } else if (part.fromIndex < fromIndex) {
                parts[index] = part.copy(
                    toIndex = fromIndex - 1
                )
                parts.add(index + 1, update(part.copy(fromIndex = fromIndex)))
            } else if (part.toIndex >= toIndex) {
                parts[index] = update(part.copy(toIndex = toIndex - 1))
                parts.add(index + 1, part.copy(fromIndex = toIndex))
            } else {
                parts[index] = update(part)
            }
        }
    }


    private fun collapseParts(
        textLastIndex: Int
    ) {
        val startRangeMap = mutableMapOf<Int, Int>()
        val endRangeMap = mutableMapOf<Int, Int>()
        val removedIndexes = mutableSetOf<Int>()

        val partsCopy = parts.toMutableList() // Create a copy of the original parts

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
                if (partsCopy[otherRangeIndex].styles == part.styles) {
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
                if (partsCopy[otherRangeIndex].styles == part.styles) {
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

        removedIndexes.reversed().forEach { partsCopy.removeAt(it) }

        // Replace the original parts with the modified copy
        parts.clear()
        parts.addAll(partsCopy)
    }

    internal fun merge(nextItem: RichTextValue): RichTextValue {
        val text = this.text + "\n" + nextItem.text
        val existingParts = ArrayList(this.parts)
        this.parts.addAll(nextItem.parts)
        forwardParts(existingParts.size, this.parts.size, this.text.length + 1)
        val newTextField = TextFieldValue(text, selection = TextRange(text.length))
        updateAnnotatedString(newTextField)
        return this
    }

    fun hasStyle(style: RichTextAttribute) = currentStyles.any { it.key == style.key }
}