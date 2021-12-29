package com.singularitycoder.viewmodelstuff2.repository

import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.utils.FavAnimeDao
import com.singularitycoder.viewmodelstuff2.utils.RetrofitService
import java.lang.Exception
import java.net.HttpURLConnection
import javax.inject.Inject

class FavAnimeRepository @Inject constructor(
    private val dao: FavAnimeDao,
    private val retrofit: RetrofitService
) {

    suspend fun getAnimeListFromRetrofit(): AnimeList? {
        return try {
            val response = retrofit.getAnimeList()

            if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
                response.body() ?: return null
                response.body()
            } else {
                null
            }

        } catch (e: Exception) {
            println(e)
            null
        }
    }



}