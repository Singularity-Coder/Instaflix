package com.singularitycoder.viewmodelstuff2.utils

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// This will be Anime filters

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

}

data class SvmValve<K, V>(
    val route: K,
    val data: V
)

enum class AnimeRoute {
    NONE,
    MAIN
}
