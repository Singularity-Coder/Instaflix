package com.singularitycoder.viewmodelstuff2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.db.FavAnimeDao
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.repository.FavAnimeRepository
import com.singularitycoder.viewmodelstuff2.utils.Utils
import com.singularitycoder.viewmodelstuff2.utils.network.ApiState
import com.singularitycoder.viewmodelstuff2.utils.network.LoadingState
import com.singularitycoder.viewmodelstuff2.utils.network.NetworkState
import com.singularitycoder.viewmodelstuff2.utils.network.RetrofitService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

// Make sure you dont have any activity context passed into ViewModels as ViewModels outlive activities.
// @HiltViewModel annotated class should contain exactly one @Inject annotated constructor. So it should have an @Inject constructor

// Accessing string resource is an issue as viewmodels ignore config change, lang chnage is ignored - https://medium.com/androiddevelopers/locale-changes-and-the-androidviewmodel-antipattern-84eb677660d9
// The recommended practice is to avoid dealing with objects that have a lifecycle in ViewModels. Use data binding or use another livedata to update resource
// https://stackoverflow.com/questions/47628646/how-should-i-get-resourcesr-string-in-viewmodel-in-android-mvvm-and-databindi

@HiltViewModel
class FavAnimeViewModel @Inject constructor(
    application: Application,
    val repository: FavAnimeRepository,
    val dao: FavAnimeDao,
    val retrofit: RetrofitService,
    val utils: Utils,
    val gson: Gson,
    val networkState: NetworkState,
) : AndroidViewModel(application) {

    private val compositeDisposable = CompositeDisposable()

    private val anime = MutableLiveData<ApiState<Anime?>>()
    private val randomAnimeList = MutableLiveData<AnimeList?>()

    // onCleared is called by the Android Activity when the activity is destroyed
    override fun onCleared() {
        Timber.d("onCleared called")
        compositeDisposable.dispose()
        super.onCleared()
    }

    // Provide access to immutable value only to views. Functions are good as they are temporary in call stack than permanently allocating space with another variable

    internal fun getAnimeList(): LiveData<ApiState<AnimeList?>> = repository.animeList

    internal fun getAnime(): LiveData<ApiState<Anime?>> = anime

    internal fun getRandomAnimeList(): LiveData<AnimeList?> = randomAnimeList

    // -------------------------------------------------------------------------------------------------------------------------------------------

    internal fun loadAnimeList() = viewModelScope.launch {
        repository.getAnimeList()
    }

    internal fun loadAnime(id: String) {
        if (networkState.isOffline()) {
//            anime.postValue(ApiState.Success(dao.getAll().value, "offline"))
            return
        }

        anime.postValue(ApiState.Loading(loadingState = LoadingState.SHOW))
        val disposable = repository.getAnime(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<Response<Anime>>() {
                override fun onSuccess(response: Response<Anime>) {
                    anime.postValue(ApiState.Success(data = response.body()))
                }

                override fun onError(error: Throwable) {
                    Timber.e(error.localizedMessage)
                    anime.postValue(ApiState.Error(message = error.localizedMessage ?: "NA"))
                }
            })
        compositeDisposable.add(disposable)
        anime.postValue(ApiState.Loading(loadingState = LoadingState.HIDE))
    }

    @ExperimentalCoroutinesApi
    internal fun loadRandomAnimeList() = viewModelScope.launch {
        randomAnimeList.postValue(repository.getRandomAnimeList())
    }
}
