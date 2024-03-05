package com.canopas.editor.jsonparser

import com.canopas.editor.ui.model.Attributes
import com.canopas.editor.ui.model.Span
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class QuillRichTextStateAdapter : JsonSerializer<Span>, JsonDeserializer<Span> {
    override fun serialize(
        src: Span?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("insert", context?.serialize(src?.insert))
        jsonObject.add("attributes", context?.serialize(src?.attributes))
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Span {
        try {
            val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")
            val insert = jsonObject.get("insert")
            val attributes = jsonObject.get("attributes")
            return Span(
                insert = context?.deserialize<String>(insert, String::class.java),
                attributes = context?.deserialize<Attributes>(attributes, Attributes::class.java)
            )
        } catch (e: Exception) {
            throw JsonParseException("Invalid JSON")
        }
    }
}
