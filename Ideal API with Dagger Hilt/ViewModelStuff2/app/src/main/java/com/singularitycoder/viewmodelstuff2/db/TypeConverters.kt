package com.singularitycoder.viewmodelstuff2.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularitycoder.viewmodelstuff2.model.Titles
import java.lang.reflect.Type
import javax.inject.Inject

class AnimeGeneresListConverter {
    private val type: Type = object : TypeToken<List<String>?>() {}.type
//    @Inject lateinit var gson: Gson   // Not injecting

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        list ?: return null
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String?): List<String>? {
        string ?: return null
        return Gson().fromJson(string, type)
    }
}

class AnimeTitleConverter {
//    @Inject lateinit var gson: Gson

    @TypeConverter
    fun objectToString(obj: Titles?): String? {
        obj ?: return null
        return Gson().toJson(obj)
    }

    @TypeConverter
    fun stringToObject(string: String?): Titles? {
        string ?: return null
        return Gson().fromJson(string, Titles::class.java)
    }
}

// https://stackoverflow.com/questions/46585075/android-how-to-make-type-converters-for-room-generic-for-all-list-of-objects
abstract class Converters<T> {
    @TypeConverter
    fun mapListToString(value: List<T>): String {
        val gson = Gson()
        val type = object : TypeToken<List<T>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun mapStringToList(value: String): List<T> {
        val gson = Gson()
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(value, type)
    }
}