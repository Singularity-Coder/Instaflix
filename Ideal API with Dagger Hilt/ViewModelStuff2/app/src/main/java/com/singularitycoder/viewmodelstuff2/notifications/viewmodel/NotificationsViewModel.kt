package com.singularitycoder.viewmodelstuff2.notifications.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.singularitycoder.viewmodelstuff2.BaseViewModel
import com.singularitycoder.viewmodelstuff2.anime.model.RandomAnimeListData
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.notifications.repository.NotificationsRepository
import com.singularitycoder.viewmodelstuff2.helpers.network.NetRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    application: Application,
    private val repository: NotificationsRepository
) : BaseViewModel<NotificationsRepository>(application, repository) {

    // Provide access to immutable value only to views. Functions are good as they are temporary in call stack than permanently allocating space with another variable

    internal fun getRandomAnimeListFromApi(): LiveData<NetRes<RandomAnimeListData?>>? = null

    internal fun getRandomAnimeListFromDb(): LiveData<NetRes<List<Notification>>> = repository.randomAnimeListFromDb

    // -------------------------------------------------------------------------------------------------------------------------------------------

    @ExperimentalCoroutinesApi
    internal fun loadRandomAnimeListFromApi() = viewModelScope.launch {
        repository.getRandomAnimeListFromApi()
    }

    @ExperimentalCoroutinesApi
    internal fun loadRandomAnimeListFromDb() = viewModelScope.launch {
        repository.getRandomAnimeListFromDb()
    }
}
