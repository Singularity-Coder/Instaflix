package com.singularitycoder.viewmodelstuff2.helpers.network

import com.singularitycoder.viewmodelstuff2.anime.model.*
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitAnimeService {

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

    @HTTP(method = "GET", path = "/v1/anime/{id}", hasBody = false)
    fun getAnime(
        /*@Header("Authorization") authToken: String = BuildConfig.ANI_API_AUTH_TOKEN*/   // Another way of adding headers
        @Path("id") id: String
    ): Single<Response<Anime>>

    @GET("/v1/anime/")
    suspend fun getFilteredAnimeList(
        @Query("title") title: String,
        @Query("anilist_id") aniListId: Int? = -1,
        @Query("mal_id") malId: Int? = null,
        @Query("formats") formats: Array<AnimeFormats>? = null,
        @Query("status") status: AnimeStatus? = null,
        @Query("year") year: Int? = null,
        @Query("season") season: AnimeSeason? = null,
        @Query("genres") genres: Array<String>? = null,
        @Query("nsfw") nsfw: Boolean
    ): Response<AnimeList>

    @GET("/v1/random/anime/")
    fun getRandomAnimeList(
        @Query(":count") count: Int,
        @Query("nsfw") nsfw: Boolean
    ): Call<RandomAnimeListData>
}
