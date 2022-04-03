package com.singularitycoder.viewmodelstuff2.helpers.animedb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularitycoder.viewmodelstuff2.anime.model.Titles
import com.singularitycoder.viewmodelstuff2.helpers.aboutmedb.ListConverters
import com.singularitycoder.viewmodelstuff2.helpers.aboutmedb.ObjectConverters
import com.singularitycoder.viewmodelstuff2.more.model.Data
import java.lang.reflect.Type

// Type converters must not contain any arguments in the constructor
// Classes that are used as TypeConverters must have no-argument public constructors.
// Use a ProvidedTypeConverter annotation if you need to take control over creating an instance of a TypeConverter.

class AnimeGeneresListConverter {
    private val type: Type = object : TypeToken<List<String>?>() {}.type
    private val gson = Gson()

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        list ?: return null
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String?): List<String>? {
        string ?: return null
        return gson.fromJson(string, type)
    }
}

class AnimeTitleConverter {
    private val gson = Gson()

    @TypeConverter
    fun objectToString(obj: Titles?): String? {
        obj ?: return null
        return gson.toJson(obj)
    }

    @TypeConverter
    fun stringToObject(string: String?): Titles? {
        string ?: return null
        return gson.fromJson(string, Titles::class.java)
    }
}

class RecommendationsListConverter {
    private val type: Type = object : TypeToken<List<Int>?>() {}.type
    private val gson = Gson()

    @TypeConverter
    fun listToString(list: List<Int>?): String? {
        list ?: return null
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String?): List<Int>? {
        string ?: return null
        return gson.fromJson(string, type)
    }
}
