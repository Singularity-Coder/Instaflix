package com.singularitycoder.viewmodelstuff2.helpers.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.viewmodelstuff2.more.dao.AboutMeDao
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.notifications.dao.NotificationsDao
import com.singularitycoder.viewmodelstuff2.helpers.constants.Db
import com.singularitycoder.viewmodelstuff2.helpers.aboutmedb.AboutMeDatabase
import com.singularitycoder.viewmodelstuff2.helpers.animedb.AnimeDatabase
import com.singularitycoder.viewmodelstuff2.helpers.animedb.MIGRATION_1_TO_2
import com.singularitycoder.viewmodelstuff2.helpers.animedb.MIGRATION_2_TO_3
import com.singularitycoder.viewmodelstuff2.helpers.animedb.MIGRATION_3_TO_4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectAnimeRoomDatabase(@ApplicationContext context: Context): AnimeDatabase {
        return Room.databaseBuilder(context, AnimeDatabase::class.java, Db.ANIME)
            .addMigrations(
                MIGRATION_1_TO_2,
                MIGRATION_2_TO_3,
                MIGRATION_3_TO_4
            )
            .build()
    }

    @Singleton
    @Provides
    fun injectAboutMeRoomDatabase(@ApplicationContext context: Context): AboutMeDatabase {
        return Room.databaseBuilder(context, AboutMeDatabase::class.java, Db.ABOUT_ME).build()
    }

    @Singleton
    @Provides
    fun injectAnimeDao(db: AnimeDatabase): AnimeDao = db.animeDao()

    @Singleton
    @Provides
    fun injectAboutMeDao(db: AboutMeDatabase): AboutMeDao = db.aboutMeDao()

    @Singleton
    @Provides
    fun injectNotificationsDao(db: AnimeDatabase): NotificationsDao = db.notificationsDao()
}
