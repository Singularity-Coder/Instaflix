package com.singularitycoder.viewmodelstuff2.favorites

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.singularitycoder.viewmodelstuff2.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Make sure you dont have any activity context passed into ViewModels as ViewModels outlive activities.
// @HiltViewModel annotated class should contain exactly one @Inject annotated constructor. So it should have an @Inject constructor

// Accessing string resource is an issue as viewmodels ignore config change, lang chnage is ignored - https://medium.com/androiddevelopers/locale-changes-and-the-androidviewmodel-antipattern-84eb677660d9
// The recommended practice is to avoid dealing with objects that have a lifecycle in ViewModels. Use data binding or use another livedata to update resource
// https://stackoverflow.com/questions/47628646/how-should-i-get-resourcesr-string-in-viewmodel-in-android-mvvm-and-databindi

// Why is it ok for viewmodel constructor params to be private? I thought Hilt needed public fields to inject. This is working in normal classes as well

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    application: Application,
    private val repository: FavoritesRepository
) : BaseViewModel<FavoritesRepository>(application, repository) {

    // Provide access to immutable value only to views. Functions are good as they are temporary in call stack than permanently allocating space with another variable

    internal fun getFavoritesLiveList(): LiveData<List<Favorite>> = repository.getFavoritesLiveList()

    // -------------------------------------------------------------------------------------------------------------------------------------------

    internal fun loadFavoriteAnimeListFromDb() = viewModelScope.launch {
        repository.getFavoritesLiveList()
    }

    internal fun addToFavorites(favorite: Favorite) = viewModelScope.launch {
        repository.addToFavorites(favorite)
    }

    internal fun removeFromFavorites(favorite: Favorite) = viewModelScope.launch {
        repository.removeToFavorites(favorite)
    }
}
