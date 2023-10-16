package com.canopas.editor.ui.parser.json

import androidx.compose.ui.text.SpanStyle
import com.canopas.editor.ui.data.RichTextSpan
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
        jsonObject.addProperty("text", src?.text)
        jsonObject.add("spans", context?.serialize(src?.spans))
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RichTextState {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")
        val text = jsonObject.get("text").asString
        val parts = context?.deserialize<MutableList<RichTextSpan>>(
            jsonObject.get("spans"),
            object : TypeToken<MutableList<RichTextSpan>>() {}.type
        )
        return RichTextState(text, parts ?: mutableListOf())
    }
}

class RichTextSpanAdapter : JsonSerializer<RichTextSpan>, JsonDeserializer<RichTextSpan> {
    override fun serialize(
        src: RichTextSpan?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("from", src?.from)
        jsonObject.addProperty("to", src?.to)
        jsonObject.addProperty("style", src?.style?.toSpansString() ?: "")
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RichTextSpan {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")
        val fromIndex = jsonObject.get("from").asInt
        val toIndex = jsonObject.get("to").asInt
        val spansString = jsonObject.get("style").asString
        val spanStyle = spansString.toSpanStyle()
        return RichTextSpan(fromIndex, toIndex, spanStyle)
    }
}

fun SpanStyle.toSpansString(): String {
    return if (this.contains(H1SPanStyle)) {
        spanStyleParserMap[H1SPanStyle] ?: ""
    } else if (this.contains(H2SPanStyle)) {
        spanStyleParserMap[H2SPanStyle] ?: ""
    } else if (this.contains(H3SPanStyle)) {
        spanStyleParserMap[H3SPanStyle] ?: ""
    } else if (this.contains(H4SPanStyle)) {
        spanStyleParserMap[H4SPanStyle] ?: ""
    } else if (this.contains(H5SPanStyle)) {
        spanStyleParserMap[H5SPanStyle] ?: ""
    } else if (this.contains(H6SPanStyle)) {
        spanStyleParserMap[H6SPanStyle] ?: ""
    } else if (this.contains(BoldSpanStyle)) {
        spanStyleParserMap[BoldSpanStyle] ?: ""
    } else if (this.contains(ItalicSpanStyle)) {
        spanStyleParserMap[ItalicSpanStyle] ?: ""
    } else if (this.contains(UnderlineSpanStyle)) {
        spanStyleParserMap[UnderlineSpanStyle] ?: ""
    } else ""


}

fun String.toSpanStyle(): SpanStyle {
    var spanStyle = SpanStyle()

    when (this.trim()) {
        spanStyleParserMap[BoldSpanStyle] -> {
            spanStyle = BoldSpanStyle
        }

        spanStyleParserMap[ItalicSpanStyle] -> {
            spanStyle = ItalicSpanStyle
        }

        spanStyleParserMap[UnderlineSpanStyle] -> {
            spanStyle = UnderlineSpanStyle
        }

        spanStyleParserMap[H1SPanStyle] -> {
            spanStyle = H1SPanStyle
        }

        spanStyleParserMap[H2SPanStyle] -> {
            spanStyle = H2SPanStyle
        }

        spanStyleParserMap[H3SPanStyle] -> {
            spanStyle = H3SPanStyle
        }

        spanStyleParserMap[H4SPanStyle] -> {
            spanStyle = H4SPanStyle
        }

        spanStyleParserMap[H5SPanStyle] -> {
            spanStyle = H5SPanStyle
        }

        spanStyleParserMap[H6SPanStyle] -> {
            spanStyle = H6SPanStyle
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