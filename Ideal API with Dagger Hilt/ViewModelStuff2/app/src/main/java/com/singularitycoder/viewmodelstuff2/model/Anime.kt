package com.singularitycoder.viewmodelstuff2.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.singularitycoder.viewmodelstuff2.utils.Avoid
import com.singularitycoder.viewmodelstuff2.utils.TABLE_ANIME_DATA
import com.singularitycoder.viewmodelstuff2.utils.TABLE_DESCRIPTIONS

// https://www.json2kotlin.com/

data class AnimeList(
    @SerializedName("status_code") val statusCode: Int,
    val message: String,
    val data: AnimeListData,
    val version: Int
)

data class AnimeListData(
    @SerializedName("current_page") val currentPage: Int,
    val count: Int,
    val documents: List<AnimeData>,
    @SerializedName("last_page") val lastPage: Int
)

data class Anime(
    @SerializedName("status_code") val statusCode: Int,
    val message: String,
    val data: AnimeData,
    val version: Int
)

@kotlinx.parcelize.Parcelize
@Entity(tableName = TABLE_ANIME_DATA)
data class AnimeData(
    @PrimaryKey @SerializedName("anilist_id") var aniListId: Int,
    // serialize true if we want to send it, deserialize true if we want to receive it. We can ignore this field to begin with. Just for show. This is now a local field. Problem with @Transient is that it ignores serialization and deserialization and we cant do just one of them and it also excludes from Room. so less control. So go with an exclusion strategy
    /*@Transient*/
    /*@Avoid(serialize = false, deserialize = true)*/
    @Ignore @Expose(serialize = false, deserialize = true) @SerializedName("mal_id") val malId: Int,
    var format: Int,
    var status: Int,
//    var titles: Titles,
//    var descriptions: Descriptions,
    @SerializedName("start_date") var startDate: String,
    @SerializedName("end_date") var endDate: String,
    @SerializedName("season_period") var seasonPeriod: Int,
    @SerializedName("season_year") var seasonYear: Int,
    @SerializedName("episodes_count") var episodesCount: Int,
    @SerializedName("episode_duration") var episodeDuration: Int,
    @SerializedName("trailer_url") var trailerUrl: String,
    @SerializedName("cover_image") var coverImage: String,
    @SerializedName("banner_image") var bannerImage: String,
//    var genres: List<String>,
    var score: Int,
    var id: Int
) : Parcelable {

    constructor() : this(
        aniListId = 0,
        malId = 0,
        format = 0,
        status = 0,
//        titles = Titles(),
//        descriptions = Descriptions(),
        startDate = "",
        endDate = "",
        seasonPeriod = 0,
        seasonYear = 0,
        episodesCount = 0,
        episodeDuration = 0,
        trailerUrl = "",
        coverImage = "",
        bannerImage = "",
//        genres = emptyList(),
        score = 0,
        id = 0
    )

    override fun equals(other: Any?): Boolean = aniListId == (other as? AnimeData)?.aniListId

    override fun toString(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)

    override fun hashCode(): Int = aniListId.hashCode()
}

//@kotlinx.parcelize.Parcelize
//@Entity(tableName = "table_titles")
//data class Titles(
//    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "title_id") val titleId: Int,
//    var en: String,
//    var jp: String,
//    var it: String
//): Parcelable {
//    constructor() : this(0, "", "", "")
//}
//
//@kotlinx.parcelize.Parcelize
//@Entity(tableName = TABLE_DESCRIPTIONS)
//data class Descriptions(
//    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "desc_id") var descId: Int,
//    var en: String,
//    var it: String
//): Parcelable {
//    constructor() : this(descId = 0, en = "", it = "")
//}
