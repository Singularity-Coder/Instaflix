package com.singularitycoder.viewmodelstuff2.anime.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.ActivityVideoAudioPlayerBinding
import dagger.hilt.android.AndroidEntryPoint

// https://www.geeksforgeeks.org/exoplayer-in-android-with-example/
// https://exoplayer.dev/

// Exo player can do audio and video streaming
// Use playlists, clip or merge media
// stream audio and video files directly from server without downloading
// Provides smooth encryption and streaming of video and audio files
// can customise media player
// supports dynamic streaming over HTTP
// works on devices >= API 16
@AndroidEntryPoint
class VideoAudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoAudioPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
