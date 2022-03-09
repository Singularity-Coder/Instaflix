package com.singularitycoder.viewmodelstuff2

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.singularitycoder.viewmodelstuff2.databinding.ActivitySplashBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.getRawPathOf
import com.singularitycoder.viewmodelstuff2.helpers.extensions.seconds
import com.singularitycoder.viewmodelstuff2.helpers.utils.doAfter
import java.util.concurrent.TimeUnit

// https://stackoverflow.com/questions/35837197/i-want-to-add-auto-play-media-in-android
// https://gist.github.com/shubhubhosale/9ef8f34a00ee41835d909c1a023b3f7a

class SplashActivity : AppCompatActivity() {

    // Excellent tool for cropping videos from landscape to portrait - https://www.veed.io/tools/rotate-video

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val splashList = listOf(R.raw.splash, R.raw.splash2)
        val rawVideoPath = getRawPathOf(video = splashList.shuffled().first())
        binding.videoView.apply {
            setVideoURI(Uri.parse(rawVideoPath))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) setTurnScreenOn(true)
            requestFocus()
            start()
            seekTo(1) // Autoplay
        }

        doAfter(2.seconds()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in)
        }
    }
}
