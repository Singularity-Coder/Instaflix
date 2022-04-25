package com.singularitycoder.viewmodelstuff2.anime.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.singularitycoder.viewmodelstuff2.databinding.ActivityExoPlayerBinding
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
class ExoPlayerActivity : AppCompatActivity() {

    // creating a variable for exoplayer
    var exoPlayer: ExoPlayer? = null

    // url of video which we are loading.
    var videoURL = "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"

    private lateinit var binding: ActivityExoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpExoPlayer()
    }

    private fun setUpExoPlayer() {
        try {
            // bandwisthmeter is used for getting default bandwidth
//            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
//
//            // track selector is used to navigate between video using a default seekbar.
//            val trackSelector: TrackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
//
//            // we are adding our track selector to exoplayer.
//            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
//
//            // we are parsing a video url and parsing its video uri.
//            val videouri: Uri = Uri.parse(videoURL)
//
//            // we are creating a variable for datasource factory and setting its user agent as 'exoplayer_view'
//            val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer_video")
//
//            // we are creating a variable for extractor factory and setting it to default extractor factory.
//            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
//
//            // we are creating a media source with above variables and passing our event handler as null,
//            val mediaSource: MediaSource = ExtractorMediaSource(videouri, dataSourceFactory, extractorsFactory, null, null)
//
//            // inside our exoplayer view we are setting our player
//            binding.idExoPlayerVIew.player = exoPlayer
//
//            // we are preparing our exoplayer with media source.
//            exoPlayer!!.prepare(mediaSource)
//
//            // we are setting our exoplayer when it is ready.
//            exoPlayer!!.playWhenReady = true
        } catch (e: Exception) {
            Log.e("TAG", "Error : $e")
        }
    }
}
