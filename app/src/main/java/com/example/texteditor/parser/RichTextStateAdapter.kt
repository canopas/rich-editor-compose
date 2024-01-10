package com.example.texteditor.parser

import com.canopas.editor.ui.model.RichTextSpan
import com.canopas.editor.ui.utils.TextSpanStyle
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class RichTextSpanAdapter : JsonSerializer<RichTextSpan>, JsonDeserializer<RichTextSpan> {
    override fun serialize(
        src: RichTextSpan?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("from", src?.from)
        jsonObject.addProperty("to", src?.to)
        jsonObject.addProperty("style", src?.style?.key ?: "")
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

fun String.toSpanStyle(): TextSpanStyle {
    return spanStyleParserMap[this] ?: TextSpanStyle.Default
}

val spanStyleParserMap = mapOf(
    "bold" to TextSpanStyle.BoldStyle,
    "italic" to TextSpanStyle.ItalicStyle,
    "underline" to TextSpanStyle.UnderlineStyle,
    "bullet" to TextSpanStyle.BulletStyle,
    "h1" to TextSpanStyle.H1Style,
    "h2" to TextSpanStyle.H2Style,
    "h3" to TextSpanStyle.H3Style,
    "h4" to TextSpanStyle.H4Style,
    "h5" to TextSpanStyle.H5Style,
    "h6" to TextSpanStyle.H6Style,
)