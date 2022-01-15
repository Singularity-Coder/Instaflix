package com.singularitycoder.viewmodelstuff2.utils.network

data class NetRes<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?): NetRes<T> {
            return NetRes(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): NetRes<T> {
            return NetRes(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): NetRes<T> {
            return NetRes(Status.LOADING, data, null)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
