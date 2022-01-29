package com.singularitycoder.viewmodelstuff2.utils.favanimedb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.anime.model.Descriptions

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
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun favAnimeDao(): AnimeDao
}

