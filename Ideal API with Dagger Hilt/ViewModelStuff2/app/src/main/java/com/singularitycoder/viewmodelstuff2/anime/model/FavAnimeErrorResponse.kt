package com.singularitycoder.viewmodelstuff2.anime.model

import com.google.gson.annotations.SerializedName

data class FavAnimeErrorResponse(
    val error: Error
)

data class Error(
    @SerializedName("code") val errorCode: String,
    val message: String
)
