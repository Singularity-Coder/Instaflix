package com.singularitycoder.viewmodelstuff2.helpers.animedb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.anime.model.Descriptions
import com.singularitycoder.viewmodelstuff2.favorites.Favorite
import com.singularitycoder.viewmodelstuff2.favorites.FavoritesDao
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.notifications.dao.NotificationsDao

@Database(
    entities = [
        AnimeData::class,
        Descriptions::class,
        Notification::class,
        Favorite::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    AnimeTitleConverter::class,
    AnimeGeneresListConverter::class,
    RecommendationsListConverter::class
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun favoritesDao(): FavoritesDao
}

