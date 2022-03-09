package com.singularitycoder.viewmodelstuff2.anime.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseViewModel
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.model.*
import com.singularitycoder.viewmodelstuff2.anime.repository.HomeRepository
import com.singularitycoder.viewmodelstuff2.more.model.AboutMeErrorResponse
import com.singularitycoder.viewmodelstuff2.helpers.network.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.helpers.utils.wait
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

// Make sure you dont have any activity context passed into ViewModels as ViewModels outlive activities.
// @HiltViewModel annotated class should contain exactly one @Inject annotated constructor. So it should have an @Inject constructor

// Accessing string resource is an issue as viewmodels ignore config change, lang chnage is ignored - https://medium.com/androiddevelopers/locale-changes-and-the-androidviewmodel-antipattern-84eb677660d9
// The recommended practice is to avoid dealing with objects that have a lifecycle in ViewModels. Use data binding or use another livedata to update resource
// https://stackoverflow.com/questions/47628646/how-should-i-get-resourcesr-string-in-viewmodel-in-android-mvvm-and-databindi

// Why is it ok for viewmodel constructor params to be private? I thought Hilt needed public fields to inject. This is working in normal classes as well

@HiltViewModel
class AnimeViewModel @Inject constructor(
    application: Application,
    private val repository: HomeRepository,
    private val dao: AnimeDao,
    private val retrofit: RetrofitAnimeService,
    private val utils: GeneralUtils,
    private val gson: Gson,
    private val networkState: NetworkState,
) : BaseViewModel<HomeRepository>(application, repository) {

    private val compositeDisposable = CompositeDisposable()

    private val anime = MutableLiveData<ApiState<Anime?>>()
    private val filteredAnimeList = MutableLiveData<NetRes<AnimeList?>>()

    // onCleared is called by the Android Activity when the activity is destroyed
    override fun onCleared() {
        Timber.d("onCleared called")
        compositeDisposable.dispose()
        super.onCleared()
    }

    // Provide access to immutable value only to views. Functions are good as they are temporary in call stack than permanently allocating space with another variable

    internal fun getAnimeList(): LiveData<ApiState<AnimeList?>> = repository.animeList

    internal fun getAnime(): LiveData<ApiState<Anime?>> = anime

    internal fun getFilteredAnimeList(): LiveData<NetRes<AnimeList?>> = filteredAnimeList

    // -------------------------------------------------------------------------------------------------------------------------------------------

    internal fun loadAnimeList() = viewModelScope.launch {
        repository.getAnimeList()
    }

    internal fun loadAnime(id: String) {
        if (networkState.isOffline()) {
            viewModelScope.launch {
                anime.postValue(ApiState.Success(Anime(data = dao.getAnimeByAniListId(id) ?: AnimeData()), "offline"))
            }
            return
        }

        anime.value = ApiState.Loading(loadingState = LoadingState.SHOW)

        val disposable = repository.getAnime(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<Response<Anime>>() {
                override fun onSuccess(response: Response<Anime>) {
                    Timber.i("Current Thread Name: ${Thread.currentThread().name}")
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        anime.value = ApiState.Success(data = response.body())
                    } else {
                        val errorMessage = utils.getErrorMessageWithGson(context = getApplication(), error = response.errorBody(), gson = gson)
                        anime.value = ApiState.Error(message = errorMessage)
                    }
                    anime.value = ApiState.Loading(loadingState = LoadingState.HIDE)
                }

                override fun onError(error: Throwable) {
                    Timber.i("Current Thread Name: ${Thread.currentThread().name}")
                    Timber.e(error.localizedMessage)
                    anime.value = ApiState.Error(message = error.localizedMessage ?: "NA")
                    anime.value = ApiState.Loading(loadingState = LoadingState.HIDE)
                }
            })

        compositeDisposable.add(disposable)
    }

    internal fun loadFilteredAnimeList(
        title: String,
        aniListId: Int?,
        malId: Int?,
        formats: Array<AnimeFormats>?,
        status: AnimeStatus?,
        year: Int?,
        season: AnimeSeason?,
        genres: Array<String>?,
        nsfw: Boolean
    ) = viewModelScope.launch {
        try {
            if (networkState.isOffline()) {
                filteredAnimeList.postValue(
                    NetRes(
                        status = Status.SUCCESS,
                        data = AnimeList().apply { data = AnimeListData().apply { documents = dao.getAll().value ?: emptyList() } },
                        message = "offline"
                    )
                )
                return@launch
            }

            filteredAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.SHOW))
            val response = repository.getFilteredAnimeList(
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

            if (response.code() == HttpURLConnection.HTTP_OK) {
                if (null == response.body() || null == response.body()?.data || response.body()?.data?.documents.isNullOrEmpty()) {
                    filteredAnimeList.postValue(NetRes(status = Status.SUCCESS, data = response.body(), message = "NA"))
                    wait()
                    filteredAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
                    return@launch
                }

                dao.deleteAll()
                dao.insertAll(response.body()?.data?.documents!!)
                filteredAnimeList.postValue(NetRes(status = Status.SUCCESS, data = response.body()))
            } else {
                val errorMessage = utils.getErrorMessageWithRetrofit<AboutMeErrorResponse>(context = getApplication(), errorResponseBody = response.errorBody())
                filteredAnimeList.postValue(NetRes(status = Status.ERROR, message = errorMessage))
            }

            wait()
            filteredAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
        } catch (e: Exception) {
            Timber.e("Something went wrong while fetching anime list: $e")
            filteredAnimeList.postValue(NetRes(status = Status.ERROR, message = e.message))
            wait()
            filteredAnimeList.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
        }
    }
}
