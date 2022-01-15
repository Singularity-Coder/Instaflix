package com.singularitycoder.viewmodelstuff2.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.db.FavAnimeDao
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.model.AnimeListData
import com.singularitycoder.viewmodelstuff2.utils.Utils
import com.singularitycoder.viewmodelstuff2.utils.network.ApiState
import com.singularitycoder.viewmodelstuff2.utils.network.LoadingState
import com.singularitycoder.viewmodelstuff2.utils.network.NetworkState
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
    val dao: FavAnimeDao,
    val retrofit: RetrofitService,
    val context: Context,
    val utils: Utils,
    val gson: Gson,
    val networkState: NetworkState
) {

    val animeList = MutableLiveData<ApiState<AnimeList?>>()

    suspend fun getAnimeList() {
        try {
            if (networkState.isOffline()) {
                animeList.postValue(ApiState.Success(
                    data = AnimeList().apply { data = AnimeListData().apply { documents = dao.getAll().value ?: emptyList() } },
                    message = context.getString(R.string.offline)
                ))
                return
            }

            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.SHOW))

            val response = retrofit.getAnimeList()
            if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
                response.body() ?: return
                response.body()?.data?.documents ?: return
                CoroutineScope(IO).launch {
                    dao.insertAll(response.body()?.data?.documents!!)
                }
                animeList.postValue(ApiState.Success(data = response.body()))
            } else {
                animeList.postValue(ApiState.Error(message = context.getString(R.string.something_is_wrong)))
            }

            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.HIDE))
        } catch (e: Exception) {
            println("Something went wrong while fetching anime list: $e")
            animeList.postValue(ApiState.Error(message = e.stackTrace.toString()))
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
