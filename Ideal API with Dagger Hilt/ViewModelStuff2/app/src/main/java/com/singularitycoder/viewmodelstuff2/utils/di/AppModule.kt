package com.singularitycoder.viewmodelstuff2.utils.di

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
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
import com.singularitycoder.viewmodelstuff2.aboutme.dao.AboutMeDao
import com.singularitycoder.viewmodelstuff2.aboutme.repository.AboutMeRepository
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.repository.AnimeRepository
import com.singularitycoder.viewmodelstuff2.utils.*
import com.singularitycoder.viewmodelstuff2.utils.DB_ANIME
import com.singularitycoder.viewmodelstuff2.utils.aboutmedb.AboutMeDatabase
import com.singularitycoder.viewmodelstuff2.utils.favanimedb.AnimeDatabase
import com.singularitycoder.viewmodelstuff2.utils.favanimedb.MIGRATION_1_TO_2
import com.singularitycoder.viewmodelstuff2.utils.favanimedb.MIGRATION_2_TO_3
import com.singularitycoder.viewmodelstuff2.utils.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// https://stackoverflow.com/questions/60829868/android-studio-does-not-rearrange-import-in-lexicographic-order
// import lexicographic ordering issue
// See hidden files in the root of the project -> find .editorconfig -> [*.{kt,kts}] -> disabled_rules = import-ordering

// @Module is the place where the dependencies are created. It provides the created objects
// You cannot inject into objects. Only classes

@Module
@InstallIn(SingletonComponent::class)   // Scope is application level for SingletonComponent as we need these dependencies across all android components and is destroyed only when app id destroyed - refer for other scopes https://developer.android.com/training/dependency-injection/hilt-android
object AppModule {

    @Singleton
    @Provides
    fun injectRetrofit(
        okHttpClient: OkHttpClient,
        gsonBuilder: GsonBuilder
    ): Retrofit {
        // https://square.github.io/retrofit/
        // Problem with this is that you have to set the @Expose annotation to each and every field in model to serialize and deserialize. Painful. Go with exclusion strategy with a custom annotation
        val gson = gsonBuilder
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        // This needs a new instance of GsonBuilder. But why?
        val gsonWithExclusionStrategy = GsonBuilder()
            .addSerializationExclusionStrategy(SerializationExclusionStrategy())
            .addDeserializationExclusionStrategy(DeserializationExclusionStrategy())
            .create()

        // Based on the type, Hilt will automatically provide the built object wherever @Inject annotation is added
        return Retrofit.Builder()
            .baseUrl(BASE_URL_ANI_API)
            .addConverterFactory(GsonConverterFactory.create(gsonWithExclusionStrategy))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    /** You must also mark it with the qualifier [GsonBuilderForRetrofit] while passing that dependency */
    @RequiresPermission(Manifest.permission.INTERNET)
    @Singleton
    @Provides
    fun injectRetrofitAnimeService(retrofit: Retrofit): RetrofitAnimeService = retrofit.create(RetrofitAnimeService::class.java)

    @RequiresPermission(Manifest.permission.INTERNET)
    @Singleton
    @Provides
    fun injectRetrofitAboutMeService(): RetrofitAboutMeService {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain: Interceptor.Chain ->
                // Adding interceptor directly on the singleton. You can add headers and Auth token directly here or u can pass them through the retrofit service interface where u define the endpoint. But not in both places
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", BuildConfig.GITHUB_GRAPH_QL_API_AUTH_TOKEN)
                chain.proceed(requestBuilder.build())
            }

        okHttpClientBuilder.addNetworkInterceptor(StethoInterceptor())

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_GITHUB)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
        return retrofit.create(RetrofitAboutMeService::class.java)
    }

    @Singleton
    @Provides
    fun injectHttpClient(
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        // authInterceptor -> This is how to inject dependencies within this module. You simply pass them as params without any annotations. Hilt handles the rest based on the type of the param
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain: Interceptor.Chain ->
                // Adding interceptor directly on the singleton. You can add headers and Auth token directly here or u can pass them through the retrofit service interface where u define the endpoint. But not in both places
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", BuildConfig.ANI_API_AUTH_TOKEN)
                chain.proceed(requestBuilder.build())
            }

        // Enable stetho if enabled in settings
        // addNetworkInterceptor() deals with interceptors that observe a single network request and response
        okHttpClientBuilder.addNetworkInterceptor(StethoInterceptor())

        // Another way to create interceptors
        okHttpClientBuilder.addInterceptor(authInterceptor)

        // Add Authenticator
        okHttpClientBuilder.authenticator(authAuthenticator)

        // Make sure this is the last interceptor. That way this logs info associated with previous interceptors as well.
        // addInterceptor() adds interceptor that observes the full span of each call: from the connection is established (if any) until after the response source is selected (either the origin server, cache, or both).
        if (BuildConfig.DEBUG) okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)

        return okHttpClientBuilder.build()
    }

    @Singleton
    @Provides
    fun injectAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor = AuthInterceptor(context = context)

    @Singleton
    @Provides
    fun injectAuthAuthenticator(
        @ApplicationContext context: Context,
//        retrofitService: RetrofitService, // Cyclic dependency BS
//        gson: Gson,
//        utils: Utils
    ): AuthAuthenticator = AuthAuthenticator(context = context /*retrofitService = retrofitService, gson = gson, utils = utils*/)

    @Singleton
    @Provides
    fun injectLoggingInterceptor(): HttpLoggingInterceptor {
        // https://howtodoinjava.com/retrofit2/logging-with-retrofit2/
        return HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message: String ->
            Timber.tag("OKHTTP LOG").d(message) // Custom Log Tag
        }).apply {
            level = HttpLoggingInterceptor.Level.HEADERS
            level = HttpLoggingInterceptor.Level.BODY
            // A way to exclude sensitive data from logs
            redactHeader("Authorization")
            redactHeader("Cookie")
        }
    }

    @Singleton
    @Provides
    fun injectAnimeRoomDatabase(@ApplicationContext context: Context): AnimeDatabase {
        return Room.databaseBuilder(context, AnimeDatabase::class.java, DB_ANIME)
            .addMigrations(
                MIGRATION_1_TO_2,
                MIGRATION_2_TO_3
            )
            .build()
    }

    @Singleton
    @Provides
    fun injectAboutMeRoomDatabase(@ApplicationContext context: Context): AboutMeDatabase {
        return Room.databaseBuilder(context, AboutMeDatabase::class.java, DB_ABOUT_ME).build()
    }

    @Singleton
    @Provides
    fun injectAnimeDao(db: AnimeDatabase): AnimeDao = db.favAnimeDao()

    @Singleton
    @Provides
    fun injectAboutMeDao(db: AboutMeDatabase): AboutMeDao = db.aboutMeDao()

    @Singleton
    @Provides
    fun injectAnimeRepository(
        animeDao: AnimeDao,
        retrofitService: RetrofitAnimeService,
        @ApplicationContext context: Context,
        utils: Utils,
        gson: Gson,
        networkState: NetworkState
    ): AnimeRepository {
        return AnimeRepository(
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
    fun injectAboutMeRepository(
        aboutMeDao: AboutMeDao,
        retrofitService: RetrofitAboutMeService,
        @ApplicationContext context: Context,
        utils: Utils,
        gson: Gson,
        networkState: NetworkState
    ): AboutMeRepository {
        return AboutMeRepository(
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
    fun injectGlide(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context).setDefaultRequestOptions(
            RequestOptions().placeholder(R.drawable.ic_launcher_foreground).error(R.drawable.ic_launcher_foreground)
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
    fun injectUtils(retrofit: Retrofit, gson: Gson): Utils = Utils(retrofit, gson)

    @Singleton
    @Provides
    fun injectHandler(): Handler = Handler(Looper.getMainLooper())

    @Singleton
    @Provides
    fun injectNetworkState(@ApplicationContext context: Context): NetworkState = NetworkState(context)
}
