package com.singularitycoder.viewmodelstuff2.helpers.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.helpers.NotificationUtils
import com.singularitycoder.viewmodelstuff2.helpers.network.AnimeGsonAdapter
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import java.security.SecureRandom
import javax.inject.Singleton

// https://stackoverflow.com/questions/60829868/android-studio-does-not-rearrange-import-in-lexicographic-order
// import lexicographic ordering issue
// See hidden files in the root of the project -> find .editorconfig -> [*.{kt,kts}] -> disabled_rules = import-ordering

// @Module is the place where the dependencies are created. It provides the created objects
// You cannot inject into objects. Only classes

// What about same dependencies for different scopes
// How to pass dependencies from one scope to another - maybe through params
// How to handle dependency cycles - A in B, B in C, C in A

@Module
@InstallIn(SingletonComponent::class)   // Scope is application level for SingletonComponent as we need these dependencies across all android components and is destroyed only when app id destroyed - refer for other scopes https://developer.android.com/training/dependency-injection/hilt-android
object GeneralModule {

    @Singleton
    @Provides
    fun injectGlide(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context).setDefaultRequestOptions(
            RequestOptions().placeholder(R.color.purple_100).error(android.R.color.holo_red_dark)
        )
    }

    @Singleton
    @Provides
    fun injectGson(
        gsonBuilder: GsonBuilder,
        animeGsonAdapter: AnimeGsonAdapter
    ): Gson {
        return gsonBuilder
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .setPrettyPrinting()
//            .registerTypeAdapter(AnimeData::class.java, animeGsonAdapter)
//            .setLenient() // To handle MalformedJsonException
            .create()
    }

    @Singleton
    @Provides
    fun injectAnimeGsonAdapter(gsonBuilder: GsonBuilder): AnimeGsonAdapter = AnimeGsonAdapter(gsonBuilder)

    @Singleton
    @Provides
    fun injectGsonBuilderCore(): GsonBuilder = GsonBuilder()

    /** [GsonBuilderForRetrofit] qualifier will help hilt differentiate what to send */
//    @Singleton
//    @Provides
//    fun injectGsonBuilderForRetrofit(): GsonBuilder = GsonBuilder()

    @Singleton
    @Provides
    fun injectGeneralUtils(retrofit: Retrofit, gson: Gson): GeneralUtils = GeneralUtils(retrofit, gson)

    @Singleton
    @Provides
    fun injectNetworkState(@ApplicationContext context: Context): NetworkState = NetworkState(context)

    @Singleton
    @Provides
    fun injectSecureRandom(): SecureRandom = SecureRandom()

    @Singleton
    @Provides
    fun injectNotificationUtils(@ApplicationContext context: Context): NotificationUtils = NotificationUtils(context)
}
