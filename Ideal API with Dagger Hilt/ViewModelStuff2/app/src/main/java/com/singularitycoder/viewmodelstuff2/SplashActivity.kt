package com.singularitycoder.viewmodelstuff2

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.singularitycoder.viewmodelstuff2.databinding.ActivitySplashBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.avoidScreenShots
import com.singularitycoder.viewmodelstuff2.helpers.extensions.fullScreen
import com.singularitycoder.viewmodelstuff2.helpers.extensions.getRawPathOf

// TODO - Do splash the right way
// https://stackoverflow.com/questions/35837197/i-want-to-add-auto-play-media-in-android
// https://gist.github.com/shubhubhosale/9ef8f34a00ee41835d909c1a023b3f7a

class SplashActivity : AppCompatActivity() {

    // Excellent tool for cropping videos from landscape to portrait - https://www.veed.io/tools/rotate-video

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        avoidScreenShots()
        fullScreen()
        setUpVideoView()
    }

    private fun setUpVideoView() {
        val splashList = listOf(
            R.raw.splash,
            R.raw.splash2,
            R.raw.splash3
        )
        val rawVideoPath = getRawPathOf(video = splashList.shuffled().first())
        binding.videoView.apply {
            setVideoURI(Uri.parse(rawVideoPath))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) setTurnScreenOn(true)
            requestFocus()
            start()
            seekTo(1) // Autoplay
            setOnCompletionListener {
                val options = ActivityOptions.makeCustomAnimation(this@SplashActivity, android.R.anim.fade_in, android.R.anim.fade_out)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent, options.toBundle())
                finish()
                // https://www.youtube.com/watch?v=0s6x3Sn4eYo
                // https://stackoverflow.com/questions/3389501/activity-transition-in-android
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
}
