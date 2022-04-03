package com.singularitycoder.viewmodelstuff2.anime.model

import android.os.Parcelable
import androidx.room.*
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table
import com.singularitycoder.viewmodelstuff2.helpers.network.Skip

// https://www.json2kotlin.com/
// For things to be testable they have to as loosely coupled as possible. So dont introduce context stuff in models.
// A class annotated with @Entity must always have a primary key

// Before u implement API, always create model first before anything else. Then the views. It makes ur job easy and fluent. It sets the flow

// @Relation - Relationships btw tables - So Anime and Genre are 2 tables
// 1. One-to-One - One Anime falls under One Genre
// 2. One-to-Many - One Anime falls under Many Genres
// 3. Many-to-Many - Many Anime fall under One Genre

data class AnimeList(
    @SerializedName("status_code") val statusCode: Int = -1,
    val message: String = "",
    var data: AnimeListData = AnimeListData(),
    val version: Int = -1
) {
    constructor() : this(statusCode = -1, message = "", data = AnimeListData(), version = -1)
}

data class AnimeListData(
    @SerializedName("current_page") val currentPage: Int = -1,
    val count: Int = -1,
    var documents: List<AnimeData> = emptyList(),
    @SerializedName("last_page") val lastPage: Int = -1
)

data class Anime(
    @SerializedName("status_code") val statusCode: Int = -1,
    val message: String = "",
    var data: AnimeData = AnimeData(),
    val version: Int = -1
)

@kotlinx.parcelize.Parcelize
@Entity(tableName = Table.ANIME_DATA)
data class AnimeData(
    @PrimaryKey @ColumnInfo(name = "aniListId", index = true) @SerializedName("anilist_id") var aniListId: Long,
    // serialize true if we want to send it, deserialize true if we want to receive it. We can ignore this field to begin with. Just for show. This is now a local field. Problem with @Transient is that it ignores serialization and deserialization and we cant do just one of them and it also excludes from Room. so less control. So go with an exclusion strategy
    /*@Transient*/
    /*@Expose(serialize = false, deserialize = true)*/
    @Ignore @Skip(serialize = false, deserialize = true) @SerializedName("mal_id") val malId: Long,
    var format: Int,
    var status: Int,
    @Embedded(prefix = "title_") var titles: Titles, // Embedding with a prefix in order to give a unique column name. We can also assign a unique column name in both the data classes but we have to assign it to every field manually. Inestead an embeded prefix is a more easy way to assign a unique prefix to the column name of a table
    @Embedded(prefix = "desc_") var descriptions: Descriptions, // What Embedded does is attach all the columns of the Descriptions table and appends them to TABLE_ANIME_DATA instead of creating a new table
    @SerializedName("start_date") var startDate: String,
    @SerializedName("end_date") var endDate: String,
    @SerializedName("season_period") var seasonPeriod: Int,
    @SerializedName("season_year") var seasonYear: Int,
    @SerializedName("episodes_count") var episodesCount: Int,
    @SerializedName("episode_duration") var episodeDuration: Int,
    @SerializedName("trailer_url") var trailerUrl: String,
    @SerializedName("cover_color") var coverColor: String,
    @SerializedName("cover_image") var coverImage: String,
    @SerializedName("banner_image") var bannerImage: String,
    @SerializedName("weekly_airing_day") var weeklyAiringDay: Int,
    var genres: List<String>,   // This must have a type converter
    var score: Int,
    var id: Int,
    var prequel: Long,
    var sequel: Long,
    var recommendations: List<Int>,
    @Skip @ColumnInfo(name = "coverImageBase64", defaultValue = "") var coverImageBase64: String = "",
    @Skip @ColumnInfo(name = "bannerImageBase64", defaultValue = "") var bannerImageBase64: String = "",
    @Skip @ColumnInfo(name = "myFavReason", defaultValue = "") var myFavReason: String = "",
    @Skip @ColumnInfo(name = "myFavReasonDate", defaultValue = "") var myFavReasonDate: String = "",
    @Skip @ColumnInfo(name = "isFavourite", defaultValue = "") var isFav: Boolean = false
) : Parcelable {

    constructor() : this(
        aniListId = 0,
        malId = 0,
        format = 0,
        status = 0,
        titles = Titles(),
        descriptions = Descriptions(),
        startDate = "",
        endDate = "",
        seasonPeriod = 0,
        seasonYear = 0,
        episodesCount = 0,
        episodeDuration = 0,
        trailerUrl = "",
        coverColor = "",
        coverImage = "",
        bannerImage = "",
        genres = emptyList(),
        score = 0,
        id = 0,
        prequel = 0,
        sequel = 0,
        weeklyAiringDay = 0,
        recommendations = emptyList()
    )

    override fun equals(other: Any?): Boolean = aniListId == (other as? AnimeData)?.aniListId

    override fun toString(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)

    override fun hashCode(): Int = aniListId.hashCode()
}

// When you embedd this, it becomes part of the parent table which in this case is TABLE_ANIME_DATA
@kotlinx.parcelize.Parcelize
data class Titles(
    var en: String? = "",
    var jp: String? = "",
    var it: String? = "",
    var rj: String? = "",
) : Parcelable {
    constructor() : this(en = "", jp = "", it = "", rj = "")
}

// Descriptions is in a one to one relationship with Anime. Each anime has a description obj
@kotlinx.parcelize.Parcelize
@Entity(
    tableName = Table.DESCRIPTIONS,
/*    foreignKeys = [
        ForeignKey(
            entity = AnimeData::class,
            parentColumns = ["aniListId"],
            childColumns = ["descId"],
            onDelete = CASCADE,
            onUpdate = CASCADE // CASCADE means each action impacts both the child and the parent tables
        )
    ],
    indices = [
        Index(value = ["descId"])
    ]*/
)
data class Descriptions(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "descId") var descId: Long = -1,   // either add index = true in the ColumnInfo or in the @Entity annotation. Also avoid autoGenerate primary key as much as possible. What if int overflows? Since it belongs to Anime Table use aniListId as primary key
    @ColumnInfo(defaultValue = "") var en: String? = "",
    @ColumnInfo(defaultValue = "") var it: String? = "",
    @ColumnInfo(defaultValue = "") var jp: String? = ""
) : Parcelable {
    constructor() : this(descId = -1, en = "", it = "")
}

data class RandomAnimeListData(
    @SerializedName("status_code") val statusCode: Int = -1,
    val message: String = "",
    var data: List<AnimeData> = emptyList(),
    val version: Int = -1
)

enum class AnimeResources(val type: Byte) {
    GENRES(type = 0),
    LOCALIZATIONS(type = 1)
}

enum class AnimeSongs(val type: Byte, val text: String) {
    OPENING(type = 0, text = "Opening"),
    ENDING(type = 1, text = "Ending"),
    NONE(type = 2, text = "None");

    companion object {
        fun getText(type: Byte?): String? = values().filter { it.type == type }.firstOrNull()?.text
    }
}

enum class AnimeFormats(val value: Byte, val text: String) {
    TV(value = 0, text = "TV"),
    TV_SHORT(value = 1, text = "TV Short"),
    MOVIE(value = 2, text = "Movie"),
    SPECIAL(value = 3, text = "Special"),
    OVA(value = 4, text = "OVA"),
    ONA(value = 5, text = "ONA"),
    MUSIC(value = 6, text = "Music");

    companion object {
        fun getText(value: Byte?): String? = values().filter { it.value == value }.firstOrNull()?.text
    }
}

enum class AnimeStatus(val value: Byte, val text: String) {
    FINISHED(value = 0, text = "Finished"),
    RELEASING(value = 1, text = "Releasing"),
    NOT_YET_RELEASED(value = 2, text = "Not Yet Released"),
    CANCELLED(value = 3, text = "Cancelled");

    companion object {
        fun getText(value: Byte?): String? = values().filter { it.value == value }.firstOrNull()?.text
    }
}

enum class AnimeSeasonPeriod(val value: Byte, val text: String) {
    WINTER(value = 0, text = "Winter"),
    SPRING(value = 1, text = "Spring"),
    SUMMER(value = 2, text = "Summer"),
    FALL(value = 3, text = "Fall"),
    UNKNOWN(value = 4, text = "Unknown");

    companion object {
        fun getText(value: Byte?): String? = values().filter { it.value == value }.firstOrNull()?.text
    }
}

enum class AnimeWeeklyAiringDay(val value: Byte, val text: String) {
    SUNDAY(value = 0, text = "Sunday"),
    MONDAY(value = 1, text = "Monday"),
    TUESDAY(value = 2, text = "Tuesday"),
    WEDNESDAY(value = 3, text = "Wednesday"),
    THURSDAY(value = 4, text = "Thursday"),
    FRIDAY(value = 5, text = "Friday"),
    SATURDAY(value = 6, text = "Saturday");

    companion object {
        fun getText(value: Byte?): String? = values().filter { it.value == value }.firstOrNull()?.text
    }
}
