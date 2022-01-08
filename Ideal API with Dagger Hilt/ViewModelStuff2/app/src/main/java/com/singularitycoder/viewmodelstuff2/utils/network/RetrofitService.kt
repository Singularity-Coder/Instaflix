package com.singularitycoder.viewmodelstuff2.utils.network

import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {

    // You can add headers and Auth token directly here or u can pass them through okHttpClient interceptor. But not in both places
    /*@Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Authorization: ${BuildConfig.ANI_API_AUTH_TOKEN}}"
    )*/
    @GET("/{version}/anime/")
    suspend fun getAnimeList(
        @Path("version") version: String = "v1" // Just for show
    ): Response<AnimeList>

    @HTTP(method = "GET", path = "/v1/anime/", hasBody = false)
    fun getAnime(
        /*@Header("Authorization") authToken: String = BuildConfig.ANI_API_AUTH_TOKEN*/   // Another way of adding headers
        @Query("id") id: String
    ): Single<Response<Anime>>

    @GET("/v1/anime/")
    suspend fun getFilteredAnimeList(
        @Query("title") title: String,
        @Query("anilist_id") aniListId: String,
        @Query("mal_id") malId: String = "",
        @Query("formats") formats: String,
        @Query("status") status: String,
        @Query("year") year: String,
        @Query("season") season: String,
        @Query("genres") genres: String,
        @Query("nsfw") nsfw: Boolean
    ): Call<AnimeList>

    @GET("/v1/anime/")
    fun getRandomAnimeList(
        @Query(":count") count: Int,
        @Query("nsfw") nsfw: Boolean
    ): Call<AnimeList>
}
