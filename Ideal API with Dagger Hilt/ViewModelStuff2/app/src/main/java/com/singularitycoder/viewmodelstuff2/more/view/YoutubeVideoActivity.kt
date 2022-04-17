package com.singularitycoder.viewmodelstuff2.more.view

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.singularitycoder.viewmodelstuff2.databinding.ActivityYoutubeVideoBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.*
import com.singularitycoder.viewmodelstuff2.helpers.extensions.isNullOrBlankOrNaOrNullString
import okhttp3.internal.filterList
import okhttp3.internal.toImmutableList
import timber.log.Timber

// https://developers.google.com/youtube/android/player
// https://guides.codepath.com/android/Streaming-Youtube-Videos-with-YouTubePlayerView
// https://www.sitepoint.com/using-the-youtube-api-to-embed-video-in-an-android-app/
// https://stackoverflow.com/questions/18175397/add-youtube-data-api-to-android-studio
// https://stackoverflow.com/questions/5712849/how-do-i-keep-the-screen-on-in-my-app
// https://guides.codepath.com/android/Streaming-Youtube-Videos-with-YouTubePlayerView
// https://stackoverflow.com/questions/7818717/why-not-use-always-androidconfigchanges-keyboardhiddenorientation
class YoutubeVideoActivity : YouTubeBaseActivity() {

    private lateinit var binding: ActivityYoutubeVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        avoidScreenShots()
        fullScreen()
        initYoutubePlayer()
    }

    override fun onResume() {
        super.onResume()
        binding.root.keepScreenOn = true
    }

    override fun onPause() {
        super.onPause()
        binding.root.keepScreenOn = false
    }

    private fun avoidScreenShots() {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun initYoutubePlayer() = binding.youtubePlayerView.initialize(
        AuthToken.YOUTUBE_API,
        object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider,
                youTubePlayer: YouTubePlayer,
                wasRestored: Boolean
            ) {
                if (!wasRestored) {
                    val youtubeVideoId = intent.getStringExtra(KEY_YOUTUBE_VIDEO_ID)
                    val youtubeVideoIdList = when (intent.getStringExtra(KEY_YOUTUBE_LIST_TYPE)) {
                        aboutMeTabsList[0] -> animeFightsList.map { it.videoId }
                        aboutMeTabsList[1] -> animeMusicList.map { it.videoId }
                        aboutMeTabsList[2] -> epicAnimeMomentsList.map { it.videoId }
                        aboutMeTabsList[3] -> otherMusicList.map { it.videoId }
                        else -> emptyList()
                    }
                    if (youtubeVideoIdList.isNullOrEmpty() || youtubeVideoIdList.size == 1) {
                        if (youtubeVideoId.isNullOrBlankOrNaOrNullString()) return
                        youTubePlayer.loadVideo(youtubeVideoId) // Load Single Video
                        return
                    }
                    youTubePlayer.apply {
                        val start = youtubeVideoIdList.indexOf(youtubeVideoId)
                        val end = youtubeVideoIdList.size
                        setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT) // Player styles: CHROMELESS, MINIMAL, DEFAULT
                        loadVideos(youtubeVideoIdList.subList(start, end)) // Set PlayList
                    }
                }
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider,
                youTubeInitializationResult: YouTubeInitializationResult
            ) = Timber.e("Youtube failed to load video")
        })
}
