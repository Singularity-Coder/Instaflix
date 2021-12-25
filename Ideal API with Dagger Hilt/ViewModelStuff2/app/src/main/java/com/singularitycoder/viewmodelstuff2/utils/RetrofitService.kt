package com.singularitycoder.viewmodelstuff2.utils

import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.Query

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

    @HTTP(method = "GET", path = "/v1/anime/", hasBody = true)
    suspend fun getAnime(
        @Query("id") id: String
    ): Call<Anime>
}