package com.singularitycoder.viewmodelstuff2.utils.network

// T is the type we pass.
// We are inheriting ApiState onto these classes in order to use them inter-changeably as the super and sub classes. So ApiState class can be proxied with Success or Loading or Error classes.
sealed class ApiState<out T> {
    data class Success<out T>(val data: T? = null, val message: String = "") : ApiState<T>()
    data class Loading<out T>(val loadingState: LoadingState = LoadingState.HIDE) : ApiState<T>()
    data class Error<out T>(val data: T? = null, val message: String = "") : ApiState<T>()
}

enum class LoadingState {
    SHOW, HIDE
}
