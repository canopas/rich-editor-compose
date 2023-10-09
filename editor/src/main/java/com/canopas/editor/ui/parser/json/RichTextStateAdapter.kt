package com.canopas.editor.ui.parser.json

import androidx.compose.ui.text.SpanStyle
import com.canopas.editor.ui.data.RichTextPart
import com.canopas.editor.ui.data.RichTextState
import com.canopas.editor.ui.utils.BoldSpanStyle
import com.canopas.editor.ui.utils.H1SPanStyle
import com.canopas.editor.ui.utils.H2SPanStyle
import com.canopas.editor.ui.utils.H3SPanStyle
import com.canopas.editor.ui.utils.H4SPanStyle
import com.canopas.editor.ui.utils.H5SPanStyle
import com.canopas.editor.ui.utils.H6SPanStyle
import com.canopas.editor.ui.utils.ItalicSpanStyle
import com.canopas.editor.ui.utils.UnderlineSpanStyle
import com.canopas.editor.ui.utils.contains
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class RichTextStateAdapter : JsonSerializer<RichTextState>, JsonDeserializer<RichTextState> {
    override fun serialize(
        src: RichTextState?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("richText", src?.text)
        jsonObject.add("parts", context?.serialize(src?.parts))
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RichTextState {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")
        val text = jsonObject.get("richText").asString
        val parts = context?.deserialize<MutableList<RichTextPart>>(
            jsonObject.get("parts"),
            object : TypeToken<MutableList<RichTextPart>>() {}.type
        )
        return RichTextState(text, parts ?: mutableListOf())
    }
}

class RichTextPartAdapter : JsonSerializer<RichTextPart>, JsonDeserializer<RichTextPart> {
    override fun serialize(
        src: RichTextPart?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("fromIndex", src?.fromIndex)
        jsonObject.addProperty("toIndex", src?.toIndex)
        jsonObject.addProperty("spanStyle", src?.spanStyle?.toSpansString() ?: "")
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RichTextPart {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")
        val fromIndex = jsonObject.get("fromIndex").asInt
        val toIndex = jsonObject.get("toIndex").asInt
        val spansString = jsonObject.get("spanStyle").asString
        val spanStyle = spansString.toSpanStyle()
        return RichTextPart(fromIndex, toIndex, spanStyle)
    }
}

fun SpanStyle.toSpansString(): String {
    val spanStrings = mutableListOf<String>()

    if (this.contains(BoldSpanStyle)) {
        spanStrings.add(spanStyleParserMap[BoldSpanStyle] ?: "")
    }
    if (this.contains(ItalicSpanStyle)) {
        spanStrings.add(spanStyleParserMap[ItalicSpanStyle] ?: "")
    }
    if (this.contains(UnderlineSpanStyle)) {
        spanStrings.add(spanStyleParserMap[UnderlineSpanStyle] ?: "")
    }

    if (this.contains(H1SPanStyle)) {
        spanStrings.add(spanStyleParserMap[H1SPanStyle] ?: "")
    }
    if (this.contains(H2SPanStyle)) {
        spanStrings.add(spanStyleParserMap[H2SPanStyle] ?: "")
    }
    if (this.contains(H3SPanStyle)) {
        spanStrings.add(spanStyleParserMap[H3SPanStyle] ?: "")
    }
    if (this.contains(H4SPanStyle)) {
        spanStrings.add(spanStyleParserMap[H4SPanStyle] ?: "")
    }
    if (this.contains(H5SPanStyle)) {
        spanStrings.add(spanStyleParserMap[H5SPanStyle] ?: "")
    }
    if (this.contains(H6SPanStyle)) {
        spanStrings.add(spanStyleParserMap[H6SPanStyle] ?: "")
    }

    return spanStrings.joinToString(",")
}

fun String.toSpanStyle(): SpanStyle {
    var spanStyle = SpanStyle()
    val spanNames = this.split(",")

    spanNames.forEach { spanName ->
        when (spanName.trim()) {
            spanStyleParserMap[BoldSpanStyle] -> {
                spanStyle += BoldSpanStyle
            }

            spanStyleParserMap[ItalicSpanStyle] -> {
                spanStyle += ItalicSpanStyle
            }

            spanStyleParserMap[UnderlineSpanStyle] -> {
                spanStyle += UnderlineSpanStyle
            }

            spanStyleParserMap[H1SPanStyle] -> {
                spanStyle += H1SPanStyle
            }

            spanStyleParserMap[H2SPanStyle] -> {
                spanStyle += H2SPanStyle
            }

            spanStyleParserMap[H3SPanStyle] -> {
                spanStyle += H3SPanStyle
            }

            spanStyleParserMap[H4SPanStyle] -> {
                spanStyle += H4SPanStyle
            }

            spanStyleParserMap[H5SPanStyle] -> {
                spanStyle += H5SPanStyle
            }

            spanStyleParserMap[H6SPanStyle] -> {
                spanStyle += H6SPanStyle
            }
        }
    }

    return spanStyle
}

val spanStyleParserMap = mapOf(
    BoldSpanStyle to "bold",
    ItalicSpanStyle to "italic",
    UnderlineSpanStyle to "underline",
    H1SPanStyle to "h1",
    H2SPanStyle to "h2",
    H3SPanStyle to "h3",
    H4SPanStyle to "h4",
    H5SPanStyle to "h5",
    H6SPanStyle to "h6",
)