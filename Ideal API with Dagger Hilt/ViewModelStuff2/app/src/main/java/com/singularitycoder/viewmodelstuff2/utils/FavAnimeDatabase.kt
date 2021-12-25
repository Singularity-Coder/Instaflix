package com.singularitycoder.viewmodelstuff2.utils

import androidx.room.Database
import androidx.room.RoomDatabase
import com.singularitycoder.viewmodelstuff2.model.AnimeData

@Database(
    entities = [
        AnimeData::class,
//        Descriptions::class,
//        Titles::class
    ],
    version = 1
)
abstract class FavAnimeDatabase : RoomDatabase() {
    abstract fun favAnimeDao(): FavAnimeDao
}