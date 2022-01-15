package com.singularitycoder.viewmodelstuff2.utils.network

import com.google.gson.*
import com.singularitycoder.viewmodelstuff2.model.AnimeData
import java.lang.reflect.Type
import javax.inject.Inject

class AnimeGsonAdapter @Inject constructor(private val gsonBuilder: GsonBuilder) : JsonSerializer<AnimeData>, JsonDeserializer<AnimeData> {

    // Object to String
    override fun serialize(
        src: AnimeData?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val gson = gsonBuilder.create()
        val element = gson.toJsonTree(src, AnimeData::class.java)
        val animeDataJsonObject = element.asJsonObject
        val animeDataJsonPrimitive = JsonPrimitive(src.toString())
        // TODO do something
        return element
    }

    // String to Object
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AnimeData {
        val gson = gsonBuilder.create()
        val animeData = gson.fromJson<AnimeData>(json, AnimeData::class.java)
        val animeDataJsonObject = json?.asJsonObject
        val animeDataJsonPrimitive = json?.asJsonPrimitive
        // TODO do something
        return animeData
    }
}
