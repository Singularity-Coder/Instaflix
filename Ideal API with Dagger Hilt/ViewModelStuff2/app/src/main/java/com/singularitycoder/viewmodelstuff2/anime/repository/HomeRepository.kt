package com.singularitycoder.viewmodelstuff2.anime.repository

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseRepository
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.model.*
import com.singularitycoder.viewmodelstuff2.helpers.network.ApiState
import com.singularitycoder.viewmodelstuff2.helpers.network.LoadingState
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.network.RetrofitAnimeService
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.helpers.utils.wait
import io.reactivex.Single
import retrofit2.Response
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val dao: AnimeDao,
    private val retrofit: RetrofitAnimeService,
    private val context: Context,
    private val utils: GeneralUtils,
    private val gson: Gson,
    private val networkState: NetworkState
) : BaseRepository() {
    val animeList = MutableLiveData<ApiState<AnimeList?>>()
    val sortedAnimeList = MutableLiveData<ApiState<AnimeList?>>()
    val mediatedAnimeList = MediatorLiveData<ApiState<AnimeList?>>()

    suspend fun prepareMediatedAnimeList(isSorted: Boolean = false) {
        mediatedAnimeList.apply {
            addSource(animeList) { it: ApiState<AnimeList?>? ->
                mediatedAnimeList.postValue(it)
            }
            addSource(sortedAnimeList) { it: ApiState<AnimeList?>? ->
                mediatedAnimeList.postValue(it)
            }
        }
    }

    suspend fun sortAnimeList() {
//        val sortedList = animeList.value.data?.data?.documents?.sortedBy { it: AnimeData -> it.aniListId }
//        sortedAnimeList.postValue(ApiState.Success(data = sortedList, "sorted"))
    }

    suspend fun getAnimeList() {
        try {
            if (networkState.isOffline()) {
                animeList.postValue(
                    ApiState.Success(
                        data = AnimeList().apply { data = AnimeListData().apply { documents = dao.getAll() } },
                        message = context.getString(R.string.offline)
                    )
                )
                wait()
                animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.HIDE))
                return
            }

            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.SHOW))

            val response = retrofit.getAnimeList()
            if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
                if (null == response.body() || null == response.body()?.data || response.body()?.data?.documents.isNullOrEmpty()) {
                    animeList.postValue(ApiState.Success(data = response.body(), message = context.getString(R.string.nothing_to_show)))
                    wait()
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

            wait()
            animeList.postValue(ApiState.Loading<AnimeList>(loadingState = LoadingState.HIDE))
        } catch (e: Exception) {
            Timber.e("Something went wrong while fetching anime list: $e")
            animeList.postValue(ApiState.Error(message = e.message ?: context.getString(R.string.something_is_wrong)))
            wait()
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
        seasonPeriod: AnimeSeasonPeriod?,
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
            seasonPeriod = seasonPeriod,
            genres = genres,
            nsfw = nsfw
        )
    }
}
