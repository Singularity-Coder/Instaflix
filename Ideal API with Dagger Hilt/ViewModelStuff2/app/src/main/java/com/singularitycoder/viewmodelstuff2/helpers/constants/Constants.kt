package com.singularitycoder.viewmodelstuff2.helpers.constants

import android.Manifest
import com.singularitycoder.viewmodelstuff2.BuildConfig
import com.singularitycoder.viewmodelstuff2.anime.view.AnimeDetailFragment
import com.singularitycoder.viewmodelstuff2.helpers.ApIAuthToken
import com.singularitycoder.viewmodelstuff2.helpers.JniDefinitions
import com.singularitycoder.viewmodelstuff2.more.model.YoutubeVideo
import com.singularitycoder.viewmodelstuff2.more.view.AboutMeFragment

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
    const val FAVORITES = "table_favorites"
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
    Manifest.permission.RECORD_AUDIO
)

enum class DateType(val value: String) {
    dd_MMM_yyyy(value = "dd MMM yyyy"),
    dd_MMM_yyyy_h_mm_a(value = "dd-MMM-yyyy h:mm a"),
    dd_MMM_yyyy_hh_mm_a(value = "dd MMM yyyy, hh:mm a"),
    dd_MMM_yyyy_hh_mm_ss_a(value = "dd MMM yyyy, hh:mm:ss a"),
    dd_MMM_yyyy_h_mm_ss_aaa(value = "dd MMM yyyy, h:mm:ss aaa"),
    yyyy_MM_dd_T_HH_mm_ss_SS_Z(value = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
}

enum class Tab(val tag: String) {
    HOME(tag = "AnimeFragment"),
    FAVORITES(tag = "FavoritesFragment"),
    NOTIFICATIONS(tag = "NotificationsFragment"),
    MORE(tag = "MoreFragment")
}

enum class FragmentsTags(val value: String) {
    ANIME_DETAIL(AnimeDetailFragment::class.java.simpleName),
    ABOUT(AboutMeFragment::class.java.simpleName),
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

val animeFightsList = listOf(
    YoutubeVideo(title = "Attack on Titan - Levi vs Kenny Squad - (Full Fight) HD", videoId = "CeLvx_1FBdk"),
    YoutubeVideo(title = "Attack on Titan - Levi vs. The Beast Titan", videoId = "ceQ4nhyuX2k"),
    YoutubeVideo(title = "Attack On Titan - Floch vs ALL (60 fps)", videoId = "hzp1AeiEIMg"),
    YoutubeVideo(title = "Attack On Titan - The true form of Eren's rumbling titan that destroyed world civilization", videoId = "1-WVczhtXTw"),
    YoutubeVideo(title = "Demon Slayer - Tanjiro Kamado and Tengen Uzui vs Gyutaro", videoId = "IOkz2ld1-Kk"),
    YoutubeVideo(title = "Boruto - Naruto, Sasuke, and Boruto vs Momoshiki | (Full Fight) (1080p)", videoId = "CZJBkQQTiCo")
)

val animeMusicList = listOf(
    YoutubeVideo(title = "Ost Anne Happy - Michino Timothy Kimino Kimochi", videoId = "815kbvmNWNM"),
    YoutubeVideo(title = "Aimer - Am02:00", videoId = "-Hn6O-WvD7o"),
    YoutubeVideo(title = "Aimer - Even Heaven", videoId = "BOGoJYZWmuk"),
    YoutubeVideo(title = "Attack on Titan - Best of Soundtracks", videoId = "ZUj6k5LQrJ0")
)

val otherMusicList = listOf(
    YoutubeVideo(title = "Pandit Jasraj - Vraje Vasantam", videoId = "ItAnTZjopAM"),
    YoutubeVideo(title = "James Brown - I Got The Feelin' (Extended Version)", videoId = "Cda0twzCMC4"),
    YoutubeVideo(title = "James Brown - People Get Up And Drive Your Funky Soul (Remix)", videoId = "Nl-RVIdTUVI"),
    YoutubeVideo(title = "James Brown - Ain't It Funky Now (Live At The Olympia, Paris / 1971)", videoId = "NKloz_wR6n4"),
    YoutubeVideo(title = "James Brown - Sunny (Live At The Olympia, Paris / 1971)", videoId = "cNFb7vfANVE"),
    YoutubeVideo(title = "James Brown - Get Up Offa That Thing (Release The Pressure)", videoId = "QS8FxHsw0U0"),
    YoutubeVideo(title = "James Brown - Papa Don't Take No Mess", videoId = "T0QQUps8954"),
    YoutubeVideo(title = "James Brown - Mind Power", videoId = "7naR12OPxRw"),
    YoutubeVideo(title = "James Brown - Super Bad, Pts. 1, 2 & 3 (Mono Version)", videoId = "u4ePdvrp9go"),
    YoutubeVideo(title = "James Brown - The Payback", videoId = "D7ks03zsg1o"),
    YoutubeVideo(title = "James Brown - Cold Sweat", videoId = "lSh-QQuW3lU"),
    YoutubeVideo(title = "James Brown - Papa's Got A Brand New Bag", videoId = "M7DNkovC2Tk"),
    YoutubeVideo(title = "James Brown - Your Cheatin' Heart", videoId = "fGY9i61XhEg"),
    YoutubeVideo(title = "James Brown - Money Won't Change You", videoId = "8tJ6HyUmjqk"),
    YoutubeVideo(title = "James Brown - Get It Together (Boston) 1968", videoId = "RnO8sHmkbv4"),
    YoutubeVideo(title = "Lamb of God - Walk with Me In Hell", videoId = "m4QyQk8vOCY"),
    YoutubeVideo(title = "Lamb of God - Ashes of the Wake", videoId = "PJcy3VQJA6o"),
    YoutubeVideo(title = "Lamb of God - Omerta", videoId = "-xYZM04JxnQ"),
    YoutubeVideo(title = "Lamb of God - The Faded Line", videoId = "JuRRnVqv2Vc"),
    YoutubeVideo(title = "Lamb of God - 11th Hour", videoId = "beltaLLilj4"),
    YoutubeVideo(title = "Lamb of God - Descending", videoId = "_V_ZbJE5oLs"),
    YoutubeVideo(title = "Lamb of God - Blacken the Cursed Sun", videoId = "_oGY68hJbs4"),
    YoutubeVideo(title = "Lamb of God - As the Palaces Burn", videoId = "eWVrdFrpXHE")
)
 
val animeSakugaList = listOf(
    YoutubeVideo(title = "Attack on Titan - The Owl", videoId = "gLtky3DaBO0")
)

// https://stackoverflow.com/questions/37833395/kotlin-annotation-intdef
//const val GERMAN = "de"
//const val JAPANESE = "jp"
//@Retention(AnnotationRetention.SOURCE)
//@IntDef(GERMAN, JAPANESE)
//annotation class Language
