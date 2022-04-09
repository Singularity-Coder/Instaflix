package com.singularitycoder.viewmodelstuff2.more.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YoutubeVideo(
    val title: String,
    val videoId: String
) : Parcelable
