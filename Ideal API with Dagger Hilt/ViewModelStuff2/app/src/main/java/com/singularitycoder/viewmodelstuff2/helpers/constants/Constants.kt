package com.singularitycoder.viewmodelstuff2.helpers.constants

import android.Manifest
import com.singularitycoder.viewmodelstuff2.BuildConfig
import com.singularitycoder.viewmodelstuff2.anime.view.AnimeDetailFragment
import com.singularitycoder.viewmodelstuff2.helpers.ApIAuthToken
import com.singularitycoder.viewmodelstuff2.helpers.JniDefinitions

object AuthToken {
    val ANI_API = JniDefinitions.getApIAuthToken(apiType = ApIAuthToken.ANI_API.value)
    val GITHUB_GRAPH_QL_API = JniDefinitions.getApIAuthToken(apiType = ApIAuthToken.GITHUB.value)
}

object Db {
    const val ANIME = "db_anime"
    const val ABOUT_ME = "db_about_me"
}

object Table {
    const val ANIME_DATA = "table_anime_data"
    const val DESCRIPTIONS = "table_descriptions"
    const val ABOUT_ME = "table_about_me"
    const val NOTIFICATIONS = "table_notifications"
}

object BaseURL {
    val ANI_API = JniDefinitions.getAniApiBaseUrl()
    val GITHUB = JniDefinitions.getGithubApiBaseUrl()
}

object IntentKey {
    const val ACTION_SHAKE = "INTENT_ACTION_SHAKE"
    const val ACTION_NOTIFICATION_BADGE = "INTENT_ACTION_NOTIFICATION_BADGE"
    const val DATA_LOAD_RANDOM_ANIME = "INTENT_DATA_LOAD_RANDOM_ANIME"
    const val DATA_SHOW_NOTIFICATION_BADGE = "INTENT_DATA_SHOW_NOTIFICATION_BADGE"
    const val ANIME_ID = "INTENT_ANIME_ID"
    const val NOTIF_WORKER_RANDOM_ANIME = "INTENT_NOTIF_WORKER_RANDOM_ANIME"
    const val NOTIF_FOREGROUND_SERVICE_RANDOM_ANIME = "INTENT_NOTIF_FOREGROUND_SERVICE_RANDOM_ANIME"
}

object WorkerTag {
    const val RANDOM_ANIME = "TAG_RANDOM_ANIME"
}

val checkThisOutList = listOf(
    "Hey, Check this out!",
    "Hey, you don't want to miss this!",
    "This is super interesting!",
    "This one is damn cool!",
    "Hey, this is crazy good!",
    "Oh. so. cool!",
    "Hey, this is just amazing!",
    "Oooooooooooooohh Yes!"
)

val mainActivityPermissions = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.WRITE_CONTACTS,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
)

enum class DateType(val value: String) {
    dd_MMM_yyyy_h_mm_a(value = "dd-MMM-yyyy h:mm a"),
    dd_MMM_yyyy_hh_mm_a(value = "dd MMM yyyy, hh:mm a"),
    dd_MMM_yyyy_hh_mm_ss_a(value = "dd MMM yyyy, hh:mm:ss a"),
    dd_MMM_yyyy_h_mm_ss_aaa(value = "dd MMM yyyy, h:mm:ss aaa")
}

enum class Tab(val tag: String) {
    HOME(tag = "AnimeFragment"),
    FAVORITES(tag = "FavoritesFragment"),
    NOTIFICATIONS(tag = "NotificationsFragment"),
    MORE(tag = "MoreFragment")
}

enum class FragmentsTags(val value: String) {
    ANIME_DETAIL(AnimeDetailFragment::class.java.simpleName),
}

enum class Language(value: String) {
    GERMAN(value = "de"),
    JAPANESE(value = "jp")
}

enum class Notif(
    val channelName: String,
    val channelId: String
) {
    ANIME_RECOMMENDATION_WORKER(
        channelName = "ANIME_RECOMMENDATION_WORKER",
        channelId = "${BuildConfig.APPLICATION_ID}.ANIME_RECOMMENDATION_WORKER"
    ),
    ANIME_FOREGROUND_SERVICE(
        channelName = "ANIME_FOREGROUND_SERVICE",
        channelId = "${BuildConfig.APPLICATION_ID}.ANIME_FOREGROUND_SERVICE"
    )
}

enum class Gender(val value: String) {
    MALE("\uD83D\uDC66  Hi Weeb"),
    FEMALE("\uD83D\uDC69  Hi Weeb")
}

// https://stackoverflow.com/questions/37833395/kotlin-annotation-intdef
//const val GERMAN = "de"
//const val JAPANESE = "jp"
//@Retention(AnnotationRetention.SOURCE)
//@IntDef(GERMAN, JAPANESE)
//annotation class Language
