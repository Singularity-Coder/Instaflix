package com.singularitycoder.viewmodelstuff2.anime.view

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.singularitycoder.viewmodelstuff2.anime.model.Episode
import com.singularitycoder.viewmodelstuff2.databinding.ActivityExoPlayerBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.more.view.KEY_YOUTUBE_VIDEO_ID
import dagger.hilt.android.AndroidEntryPoint

// https://developer.android.com/codelabs/exoplayer-intro#2
// https://exoplayer.dev/

// Exo player can do audio and video streaming
// Use playlists, clip or merge media
// stream audio and video files directly from server without downloading
// Provides smooth encryption and streaming of video and audio files
// can customise media player
// supports dynamic streaming over HTTP
// works on devices >= API 16
@AndroidEntryPoint
class ExoPlayerActivity : AppCompatActivity() {

    // url of video which we are loading.
    private val videoUrl = "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"
    private val audioUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"

    private lateinit var binding: ActivityExoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        avoidScreenShots()
        fullScreen()
        setUpExoPlayer()
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

    private fun setUpExoPlayer() {
        val videoUrl = intent.getParcelableArrayListExtra<Episode>(IntentKey.EPISODE_LIST)?.firstOrNull()?.video ?: ""
        val mediaItem = MediaItem.fromUri(videoUrl)
        val exoPlayer = ExoPlayer.Builder(this).build()
        binding.exoPlayerView.player = exoPlayer
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true // Since we are loading from url, we cannot directly set play()
    }
}
