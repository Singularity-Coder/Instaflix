package com.singularitycoder.viewmodelstuff2.helpers.di

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.singularitycoder.viewmodelstuff2.BuildConfig
import com.singularitycoder.viewmodelstuff2.helpers.constants.AuthToken
import com.singularitycoder.viewmodelstuff2.helpers.constants.BaseURL
import com.singularitycoder.viewmodelstuff2.helpers.network.*
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

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

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
            .baseUrl(BaseURL.ANI_API)
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
                    .addHeader("Authorization", AuthToken.GITHUB_GRAPH_QL_API)
                chain.proceed(requestBuilder.build())
            }

        okHttpClientBuilder.addNetworkInterceptor(StethoInterceptor())

        val retrofit = Retrofit.Builder()
            .baseUrl(BaseURL.GITHUB)
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
//        utils: GeneralUtils
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
}
