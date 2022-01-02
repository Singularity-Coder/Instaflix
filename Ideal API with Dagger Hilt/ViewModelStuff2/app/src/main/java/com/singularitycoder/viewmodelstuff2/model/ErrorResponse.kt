package com.singularitycoder.viewmodelstuff2.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    val error: Error
)

data class Error(
    @SerializedName("code") val errorCode: String,
    val message: String
)