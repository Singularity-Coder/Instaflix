package com.singularitycoder.viewmodelstuff2.helpers.di

import android.content.Context
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.more.dao.AboutMeDao
import com.singularitycoder.viewmodelstuff2.more.repository.MoreRepository
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.repository.HomeRepository
import com.singularitycoder.viewmodelstuff2.notifications.dao.NotificationsDao
import com.singularitycoder.viewmodelstuff2.notifications.repository.NotificationsRepository
import com.singularitycoder.viewmodelstuff2.helpers.NotificationUtils
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.network.RetrofitAboutMeService
import com.singularitycoder.viewmodelstuff2.helpers.network.RetrofitAnimeService
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun injectHomeRepository(
        animeDao: AnimeDao,
        retrofitService: RetrofitAnimeService,
        @ApplicationContext context: Context,
        utils: GeneralUtils,
        gson: Gson,
        networkState: NetworkState
    ): HomeRepository {
        return HomeRepository(
            dao = animeDao,
            retrofit = retrofitService,
            context = context,
            utils = utils,
            gson = gson,
            networkState = networkState
        )
    }

    @Singleton
    @Provides
    fun injectMoreRepository(
        aboutMeDao: AboutMeDao,
        retrofitService: RetrofitAboutMeService,
        @ApplicationContext context: Context,
        utils: GeneralUtils,
        gson: Gson,
        networkState: NetworkState
    ): MoreRepository {
        return MoreRepository(
            dao = aboutMeDao,
            retrofit = retrofitService,
            context = context,
            utils = utils,
            gson = gson,
            networkState = networkState
        )
    }

    @Singleton
    @Provides
    fun injectNotificationsRepository(
        notificationsDao: NotificationsDao,
        retrofitService: RetrofitAnimeService,
        @ApplicationContext context: Context,
        utils: GeneralUtils,
        gson: Gson,
        networkState: NetworkState,
        secureRandom: SecureRandom,
        notificationUtils: NotificationUtils
    ): NotificationsRepository {
        return NotificationsRepository(
            dao = notificationsDao,
            retrofit = retrofitService,
            context = context,
            utils = utils,
            gson = gson,
            networkState = networkState,
            secureRandom = secureRandom,
            notificationUtils = notificationUtils
        )
    }
}
