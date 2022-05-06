package com.singularitycoder.viewmodelstuff2.anime.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class EpisodeList(
    @SerializedName("status_code") val statusCode: Int?,
    val message: String?,
    val data: EpisodeListData?
)

data class EpisodeListData(
    @SerializedName("current_page") val currentPage: Int?,
    val count: Int?,
    val documents: List<Episode>?
)

@kotlinx.parcelize.Parcelize
data class Episode(
    @SerializedName("anime_id") val animeId: Int?,
    val number: Int?,
    val title: String?,
    val video: String?,
    @SerializedName("video_headers") val videoHeaders: VideoHeaders?,
    val quality: String,
    val format: String,
    val locale: String,
    @SerializedName("is_dub") val isDub: Boolean?,
    val id: Int?
) : Parcelable

@kotlinx.parcelize.Parcelize
data class VideoHeaders(
    val host: String?,
    val referer: String?
) : Parcelable
