package com.singularitycoder.viewmodelstuff2.di

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.singularitycoder.viewmodelstuff2.BuildConfig
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.repository.FavAnimeRepository
import com.singularitycoder.viewmodelstuff2.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// @Module is the place where the dependencies are created

@Module
@InstallIn(SingletonComponent::class)   // Scope is application level for SingletonComponent as we need these dependencies across all android components and is destroyed only when app id destroyed - refer for other scopes https://developer.android.com/training/dependency-injection/hilt-android
object AppModule {

    @RequiresPermission(Manifest.permission.INTERNET)
    @Singleton
    @Provides
    fun injectRetrofitService(): RetrofitService {

        // Problem with this is that you have to set the @Expose annotation to each and every field in model to serialize and deserialize. Painful
        val gson = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        val gsonWithExclusionStrategy = GsonBuilder()
            .addSerializationExclusionStrategy(SerializationExclusionStrategy())
            .addDeserializationExclusionStrategy(DeserializationExclusionStrategy())
            .create()

        // Since this is used only once, no need to inject this
        fun getHttpClientBuilder(): OkHttpClient {
            val okHttpClientBuilder = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor { it: Interceptor.Chain ->
                    // You can add headers and Auth token directly here or u can pass them through the retrofit service interface where u define the endpoint. But not in both places
                    val requestBuilder = it.request().newBuilder().apply {
                        addHeader("Content-Type", "application/json")
                        addHeader("Accept", "application/json")
                        addHeader("Authorization", BuildConfig.ANI_API_AUTH_TOKEN)
                    }
                    it.proceed(requestBuilder.build())
                }

            // Enable stetho if enabled in settings
            okHttpClientBuilder.addInterceptor(StethoInterceptor())

            return okHttpClientBuilder.build()
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gsonWithExclusionStrategy))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(getHttpClientBuilder())
            .build()
            .create(RetrofitService::class.java)
    }

    @Singleton
    @Provides
    fun injectRoomDatabase(@ApplicationContext context: Context): FavAnimeDatabase {
        return Room.databaseBuilder(context, FavAnimeDatabase::class.java, DB_FAV_ANIME).build()
    }

    @Singleton
    @Provides
    fun injectRoomDao(db: FavAnimeDatabase): FavAnimeDao {
        return db.favAnimeDao()
    }

    @Singleton
    @Provides
    fun injectRepository(favAnimeDao: FavAnimeDao, retrofitService: RetrofitService): FavAnimeRepository {
        return FavAnimeRepository(dao = favAnimeDao, retrofit = retrofitService)
    }

    @Singleton
    @Provides
    fun injectGlide(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context).setDefaultRequestOptions(
            RequestOptions().placeholder(R.drawable.ic_launcher_foreground).error(R.drawable.ic_launcher_foreground)
        )
    }

    @Singleton
    @Provides
    fun injectGson(): Gson = Gson()
}