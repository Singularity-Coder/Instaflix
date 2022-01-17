package com.singularitycoder.viewmodelstuff2.utils.network

data class NetRes<out T>(val status: Status, val data: T? = null, val message: String? = "", val loadingState: LoadingState = LoadingState.HIDE) {

    companion object {
        fun <T> success(data: T?, message: String): NetRes<T> = NetRes(status = Status.SUCCESS, data = data, message = message)
        fun <T> error(data: T?, message: String): NetRes<T> = NetRes(status = Status.ERROR, data = data, message = message)
        fun <T> loading(loadingState: LoadingState = LoadingState.HIDE): NetRes<T> = NetRes(status = Status.LOADING, loadingState = loadingState)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
