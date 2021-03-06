package com.singularitycoder.viewmodelstuff2.helpers.network

import com.singularitycoder.viewmodelstuff2.helpers.ShowLoader
import com.singularitycoder.viewmodelstuff2.helpers.TwoArgMethod

// T is the type we pass.
// We are inheriting ApiState onto these classes in order to use them inter-changeably as the super and sub classes. So ApiState class can be proxied with Success or Loading or Error classes.
sealed class ApiState<out T> {
    data class Success<out T>(val data: T? = null, val message: String = "") : ApiState<T>()
    data class Loading<out T>(val loadingState: LoadingState = LoadingState.HIDE) : ApiState<T>()
    data class Error<out T>(val data: T? = null, val message: String = "") : ApiState<T>()
}

inline infix fun <T> ApiState<T>.onSuccess(action: TwoArgMethod<T, String>): ApiState<T> {
    if (this is ApiState.Success) action.invoke(this.data, this.message)
    return this
}

inline infix fun <T> ApiState<T>.onFailure(action: TwoArgMethod<T, String>): ApiState<T> {
    if (this is ApiState.Error) action.invoke(this.data, this.message)
    return this
}

inline infix fun <T> ApiState<T>.onLoading(action: ShowLoader): ApiState<T> {
    if (this is ApiState.Loading) action.invoke(loadingState)
    return this
}

enum class LoadingState {
    SHOW, HIDE
}
