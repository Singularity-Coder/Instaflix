package com.singularitycoder.viewmodelstuff2.repository

import android.content.Context
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.db.FavAnimeDao
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.utils.Utils
import com.singularitycoder.viewmodelstuff2.utils.network.RetrofitService
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class FavAnimeRepository @Inject constructor(
    private val dao: FavAnimeDao,
    private val retrofit: RetrofitService,
    private val context: Context
) {

    @Inject lateinit var utils: Utils
    @Inject lateinit var gson: Gson

    suspend fun getAnimeList(): AnimeList? {
        return try {
            val response = retrofit.getAnimeList()
            if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
                response.body() ?: return null
                response.body()?.data?.documents ?: return null
                CoroutineScope(IO).launch {
                    dao.insertAll(response.body()?.data?.documents!!)
                }
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Something went wrong while fetching anime list: $e")
            null
        }
    }

    fun getAnime(id: String): Single<Response<Anime>> = retrofit.getAnime(id)

    @ExperimentalCoroutinesApi
    suspend fun getRandomAnimeList(): AnimeList? = suspendCancellableCoroutine<AnimeList?> { continuation ->
        retrofit.getRandomAnimeList(0, true).enqueue(object : Callback<AnimeList> {
            override fun onResponse(call: Call<AnimeList>, response: Response<AnimeList>) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    if (continuation.isActive) continuation.resume(response.body(), null)
                } else {
                    val errorMessage = utils.getErrorMessageWithRetrofit(context = context, errorResponseBody = response.errorBody())
                }
            }

            override fun onFailure(call: Call<AnimeList>, t: Throwable) = Unit
        })
    }
}
