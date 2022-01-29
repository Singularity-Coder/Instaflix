package com.singularitycoder.viewmodelstuff2.aboutme.model

import com.google.gson.annotations.SerializedName

data class AboutMeErrorResponse(
    val message: String,
    @SerializedName("documentation_url") val documentationUrl: String
)

