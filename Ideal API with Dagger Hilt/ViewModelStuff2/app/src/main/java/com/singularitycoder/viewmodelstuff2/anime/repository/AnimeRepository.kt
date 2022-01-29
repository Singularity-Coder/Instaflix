package com.singularitycoder.viewmodelstuff2.anime.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.model.*
import com.singularitycoder.viewmodelstuff2.utils.Utils
import com.singularitycoder.viewmodelstuff2.utils.network.*
import io.reactivex.Single
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

class AnimeRepository @Inject constructor(
    private val dao: AnimeDao,
    private val retrofit: RetrofitAnimeService,
    private val context: Context,
    private val utils: Utils,
    private val gson: Gson,
    private val networkState: NetworkState
) {
    val animeList = MutableLiveData<ApiState<AnimeList?>>()
    val randomAnimeList = MutableLiveData<NetRes<AnimeList?>>()

    suspend fun getAnimeList() {
        try {
            if (networkState.isOffline()) {
                animeList.postValue(
                    ApiState.Success(
                        data = AnimeList().apply { data = AnimeListData().apply { documents = dao.getAll().value ?: emptyList() } },
                        message = context.getString(R.string.offline)
                    )
                )
                return
            }

            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.SHOW))

            val response = retrofit.getAnimeList()
            if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
                if (null == response.body() || null == response.body()?.data || response.body()?.data?.documents.isNullOrEmpty()) {
                    animeList.postValue(ApiState.Success(data = response.body(), message = context.getString(R.string.nothing_to_show)))
                    utils.delayUntilNextPostValue()
                    animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.HIDE))
                    return
                }

                dao.deleteAll()
                dao.insertAll(response.body()?.data?.documents!!)
                animeList.postValue(ApiState.Success(data = response.body()))
            } else {
                val errorMessage = utils.getErrorMessageWithRetrofit<AnimeErrorResponse>(context = context, errorResponseBody = response.errorBody())
                animeList.postValue(ApiState.Error(message = errorMessage))
            }

            utils.delayUntilNextPostValue()
            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.HIDE))
        } catch (e: Exception) {
            Timber.e("Something went wrong while fetching anime list: $e")
            animeList.postValue(ApiState.Error(message = e.message ?: context.getString(R.string.something_is_wrong)))
            utils.delayUntilNextPostValue()
            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.HIDE))
        }
    }

    fun getAnime(id: String): Single<Response<Anime>> = retrofit.getAnime(id)

    suspend fun getFilteredAnimeList(
        title: String,
        aniListId: Int?,
        malId: Int?,
        formats: Array<AnimeFormats>?,
        status: AnimeStatus?,
        year: Int?,
        season: AnimeSeason?,
        genres: Array<String>?,
        nsfw: Boolean
    ): Response<AnimeList> {
        return retrofit.getFilteredAnimeList(
            title = title,
            aniListId = aniListId,
            malId = malId,
            formats = formats,
            status = status,
            year = year,
            season = season,
            genres = genres,
            nsfw = nsfw
        )
    }

    /** Will be used in Work Manager. Polls this every 15 min. So loading is unnecessary */
    @ExperimentalCoroutinesApi
    suspend fun getRandomAnimeList(): LiveData<NetRes<AnimeList?>> = suspendCancellableCoroutine<LiveData<NetRes<AnimeList?>>> { continuation ->
        if (networkState.isOffline()) {
            randomAnimeList.postValue(
                NetRes(
                    status = Status.SUCCESS,
                    data = AnimeList().apply { data = AnimeListData().apply { documents = dao.getAll().value ?: emptyList() } },
                    message = context.getString(R.string.offline)
                )
            )
            if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
            return@suspendCancellableCoroutine
        }

        randomAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.SHOW))
        if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)

        retrofit.getRandomAnimeList(0, true).enqueue(object : Callback<AnimeList> {
            override fun onResponse(call: Call<AnimeList>, response: Response<AnimeList>) {
                Timber.i("Current Thread Name: ${Thread.currentThread().name}")
                CoroutineScope(IO).launch {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        if (null == response.body() || null == response.body()?.data || response.body()?.data?.documents.isNullOrEmpty()) {
                            randomAnimeList.postValue(NetRes(status = Status.SUCCESS, message = context.getString(R.string.nothing_to_show)))
                            if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
                            utils.delayUntilNextPostValue()
                            randomAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
                            if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
                            return@launch
                        }
                        dao.deleteAll()
                        dao.insertAll(response.body()?.data?.documents!!)

                        randomAnimeList.postValue(NetRes(status = Status.SUCCESS, data = response.body()))
                        if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
                    } else {
                        val errorMessage = utils.getErrorMessageWithRetrofit<AnimeErrorResponse>(context = context, errorResponseBody = response.errorBody())
                        randomAnimeList.postValue(NetRes(status = Status.ERROR, message = errorMessage))
                        if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
                    }

                    utils.delayUntilNextPostValue()
                    randomAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
                    if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
                }
            }

            override fun onFailure(call: Call<AnimeList>, t: Throwable) {
                Timber.i("Current Thread Name: ${Thread.currentThread().name}")
                Timber.e("Something went wrong while fetching anime list: ${t.message}")
                randomAnimeList.value = NetRes(status = Status.ERROR, message = t.message)
                if (continuation.isActive) continuation.resume(value = randomAnimeList, onCancellation = null)
            }
        })
    }
}
