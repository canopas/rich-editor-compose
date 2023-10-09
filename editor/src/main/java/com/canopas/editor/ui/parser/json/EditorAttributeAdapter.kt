package com.canopas.editor.ui.parser.json

import com.canopas.editor.ui.data.EditorAttribute
import com.canopas.editor.ui.data.RichTextState
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class EditorAttributeAdapter : JsonSerializer<EditorAttribute>, JsonDeserializer<EditorAttribute> {
    override fun serialize(
        src: EditorAttribute?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()

        when (src) {
            is EditorAttribute.ImageAttribute -> {
                jsonObject.addProperty("type", src.type)
                jsonObject.addProperty("value", src.url)
            }

            is EditorAttribute.VideoAttribute -> {
                jsonObject.addProperty("type", src.type)
                jsonObject.addProperty("value", src.url)
            }

            is EditorAttribute.TextAttribute -> {
                jsonObject.addProperty("type", src.type)
                jsonObject.add("richText", context?.serialize(src.richText))
            }

            else -> {}
        }

        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EditorAttribute? {
        if (json == null || !json.isJsonObject) {
            return null
        }

        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString

        return when (type) {
            "image" -> EditorAttribute.ImageAttribute(jsonObject.get("value").asString)
            "video" -> EditorAttribute.VideoAttribute(jsonObject.get("value").asString)
            "text" -> EditorAttribute.TextAttribute(
                context?.deserialize(jsonObject.get("richText"), RichTextState::class.java)
                    ?: RichTextState()
            )

            else -> throw JsonParseException("Unknown EditorAttribute type: $type")
        }
    }
}
