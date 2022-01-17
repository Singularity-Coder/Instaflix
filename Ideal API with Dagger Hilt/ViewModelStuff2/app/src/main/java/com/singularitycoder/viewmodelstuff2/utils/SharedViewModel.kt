package com.singularitycoder.viewmodelstuff2.utils

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

}

data class SvmValve<T, V>(
    val route: T,
    val data: V
)

enum class AnimeRoute {
    NONE,
    MAIN
}
