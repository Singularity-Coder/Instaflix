package com.singularitycoder.viewmodelstuff2.more.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.singularitycoder.viewmodelstuff2.BaseViewModel
import com.singularitycoder.viewmodelstuff2.more.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.more.repository.MoreRepository
import com.singularitycoder.viewmodelstuff2.helpers.network.ApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    application: Application,
    private val repository: MoreRepository,
) : BaseViewModel<MoreRepository>(application, repository) {

    private val aboutMe = repository.aboutMe

    internal fun getAboutMe(): LiveData<ApiState<GitHubProfileQueryModel?>> = aboutMe

    internal fun loadAboutMe() = viewModelScope.launch {
        repository.getAboutMe()
    }
}
