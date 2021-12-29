package com.singularitycoder.viewmodelstuff2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.repository.FavAnimeRepository
import com.singularitycoder.viewmodelstuff2.utils.RetrofitService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavAnimeViewModel @Inject constructor(
    private val repository: FavAnimeRepository,
    private val retrofit: RetrofitService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val animeList = MutableLiveData<AnimeList>()
    private val anime = MutableLiveData<Anime>()

    // onCleared is called by the Android Activity when the activity is destroyed
    override fun onCleared() {
        Timber.d("onCleared called")
        compositeDisposable.dispose()
        super.onCleared()
    }

    internal fun getAnimeList(): LiveData<AnimeList> = animeList

    internal fun loadAnimeList() = viewModelScope.launch {
        animeList.postValue(repository.getAnimeListFromRetrofit())
    }

    internal fun loadAnime(id: String) {
        val disposable = retrofit.getAnime(id)
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
}