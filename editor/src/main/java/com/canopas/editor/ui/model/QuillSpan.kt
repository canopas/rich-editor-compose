package com.canopas.editor.ui.model

data class QuillSpan(
    val spans: List<Span>
)

data class Span(
    var insert: String?,
    val attributes: Attributes? = null
)

data class Attributes(
    val header: Int? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val list: ListType? = null
)

enum class ListType {
    ordered, bullet
}