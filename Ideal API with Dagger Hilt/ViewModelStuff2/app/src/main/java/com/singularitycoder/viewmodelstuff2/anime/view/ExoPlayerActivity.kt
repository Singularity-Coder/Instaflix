package com.singularitycoder.viewmodelstuff2.anime.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.singularitycoder.viewmodelstuff2.anime.model.Episode
import com.singularitycoder.viewmodelstuff2.databinding.ActivityExoPlayerBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.extensions.avoidScreenShots
import com.singularitycoder.viewmodelstuff2.helpers.extensions.fullScreen
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

    private fun setUpExoPlayer() {
        val episodeList = try {
            intent.getParcelableArrayListExtra<Episode?>(IntentKey.EPISODE_LIST) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val mediaItemsList = episodeList.mapNotNull { it: Episode -> MediaItem.fromUri(it.video ?: "") }
        val exoPlayer = ExoPlayer.Builder(this).build().also { it: ExoPlayer ->
            binding.exoPlayerView.player = it
        }.apply {
            addMediaItems(mediaItemsList)
            prepare()
            playWhenReady = true // Since we are loading from url, we cannot directly set play()
        }
    }
}
