package com.singularitycoder.viewmodelstuff2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.repository.FavAnimeRepository
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

@HiltViewModel
class FavAnimeViewModel @Inject constructor(
    private val repository: FavAnimeRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val animeList = MutableLiveData<AnimeList?>()
    private val anime = MutableLiveData<Anime?>()
    private val randomAnimeList = MutableLiveData<AnimeList?>()

    // onCleared is called by the Android Activity when the activity is destroyed
    override fun onCleared() {
        Timber.d("onCleared called")
        compositeDisposable.dispose()
        super.onCleared()
    }

    // Provide access to immutable value only to views. Functions are good as they are temporary in call stack than permanently storing in another variable

    internal fun getAnimeList(): LiveData<AnimeList?> = animeList

    internal fun getAnime(): LiveData<Anime?> = anime

    internal fun getRandomAnimeList(): LiveData<AnimeList?> = randomAnimeList

    // -------------------------------------------------------------------------------------------------------------------------------------------

    internal fun loadAnimeList() = viewModelScope.launch {
        val aniList = repository.getAnimeList()
        animeList.postValue(aniList)
    }

    internal fun loadAnime(id: String) {
        val disposable = repository.getAnime(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<Response<Anime>>() {
                override fun onSuccess(response: Response<Anime>) {
                    anime.postValue(response.body())
                }

                override fun onError(error: Throwable) {
                    Timber.e(error.localizedMessage)
                }
            })
        compositeDisposable.add(disposable)
    }

     @ExperimentalCoroutinesApi
     internal fun loadRandomAnimeList() = viewModelScope.launch {
         randomAnimeList.postValue(repository.getRandomAnimeList())
     }
}
