package com.singularitycoder.viewmodelstuff2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.repository.FavAnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavAnimeViewModel @Inject constructor(
    private val repository: FavAnimeRepository
) : ViewModel() {

    private val animeList = MutableLiveData<AnimeList>()

    fun getAnimeList(): LiveData<AnimeList> = animeList

    fun loadAnimeList() = viewModelScope.launch {
        animeList.postValue(repository.getAnimeListFromRetrofit())
    }
}