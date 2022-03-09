package com.singularitycoder.viewmodelstuff2.helpers.aboutmedb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularitycoder.viewmodelstuff2.more.model.*

// https://stackoverflow.com/questions/46585075/android-how-to-make-type-converters-for-room-generic-for-all-list-of-objects
abstract class ListConverters<T> {
    val gson: Gson = Gson()

    @TypeConverter
    fun mapListToString(value: List<T>): String {
        val type = object : TypeToken<List<T>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun mapStringToList(value: String): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(value, type)
    }
}

abstract class ObjectConverters<T> {
    val gson: Gson = Gson()

    @TypeConverter
    fun objectToString(obj: T?): String? {
        obj ?: return null
        return gson.toJson(obj)
    }

    @TypeConverter
    fun stringToObject(string: String?): T? {
        string ?: return null
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(string, type)
    }
}

class DataConverter: ObjectConverters<Data>()
class RepositoryOwnerConverter: ObjectConverters<RepositoryOwner>()
class FollowersConverter: ObjectConverters<Followers>()
class FollowingConverter: ObjectConverters<Following>()
class PinnedItemsConverter: ObjectConverters<PinnedItems>()
class StarredRepositoriesConverter: ObjectConverters<StarredRepositories>()
class TopRepositoriesConverter: ObjectConverters<TopRepositories>()
class EdgeConverter: ObjectConverters<Edge>()
class NodeConverter: ObjectConverters<Node>()
class OwnerConverter: ObjectConverters<Owner>()
class PrimaryLanguageConverter: ObjectConverters<PrimaryLanguage>()
class NodeXConverter: ObjectConverters<NodeX>()
class OwnerXConverter: ObjectConverters<OwnerX>()
class PrimaryLanguageXConverter: ObjectConverters<PrimaryLanguageX>()
