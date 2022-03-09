package com.singularitycoder.viewmodelstuff2.helpers.di

import com.bumptech.glide.RequestManager
import com.singularitycoder.viewmodelstuff2.anime.view.HomeAdapter
import com.singularitycoder.viewmodelstuff2.notifications.view.NotificationsAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdapterModule {

    @Singleton
    @Provides
    fun injectNotificationsAdapter(requestManager: RequestManager): NotificationsAdapter = NotificationsAdapter(requestManager)

    @Singleton
    @Provides
    fun injectHomeAdapter(requestManager: RequestManager): HomeAdapter = HomeAdapter(requestManager)
}