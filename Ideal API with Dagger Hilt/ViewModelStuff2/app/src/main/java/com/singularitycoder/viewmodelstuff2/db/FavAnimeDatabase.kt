package com.singularitycoder.viewmodelstuff2.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.viewmodelstuff2.model.AnimeData
import com.singularitycoder.viewmodelstuff2.model.Descriptions

@Database(
    entities = [
        AnimeData::class,
        Descriptions::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    AnimeTitleConverter::class,
    AnimeGeneresListConverter::class
)
abstract class FavAnimeDatabase : RoomDatabase() {
    abstract fun favAnimeDao(): FavAnimeDao
}

