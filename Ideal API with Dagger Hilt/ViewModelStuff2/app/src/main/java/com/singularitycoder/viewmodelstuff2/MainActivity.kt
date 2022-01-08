package com.singularitycoder.viewmodelstuff2

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.databinding.ActivityMainBinding
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.utils.network.NetworkStateListener
import com.singularitycoder.viewmodelstuff2.viewmodel.FavAnimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO
// Online Offline ribbon with fade animation
// Network resource - loading, success, failure
// Retrofit - Interceptors
// Custom Views
// Double catch blocks
// Throw Exceptions
// Migrations - Auto Migrations
// Fix Foreign Key Issue

// Synchronized block
// @Synchronized annotation
// @Volatile
// WeakReference
// Identity equality ===

// TODO Add network listeners for API calls
// TODO Integrate Ktlint
// TODO Work manager that polls every 15 mins
// TODO Foreground Service that shows random anime suggestions
// TODO Auth token in Cpp file
// TODO Sealed Class for data models
// TODO create another module for utils
// TODO Weak Reference context for Sharedprefs
// TODO lottie placeholder
// TODO In-App rating
// TODO In-App Update
// TODO Ads - After adding an anime

// TODO Groovy to Kotlin DSL version
// TODO Compose version
// TODO Kotlin Flows version
// TODO paging version
// TODO without Hilt version

// Before u implement API, always create model first before anything else. Then the views. It makes ur job easy and fluent. It sets the flow
// Hilt constructs classes, provides containers and manages lifecycles automatically
// Hilt has compile-time correctness, runtime performance, scalabitliy,
// Hilt auto generates containers

// Hilt
// 1. Activities and Fragments must have @AndroidEntryPoint
// 2. Must have Application class annotated with @HiltAndroidApp
// @Module provides the dependency for us
// Hilt provides dependencies through the constructor - ex; ViewModels get repository from its constructor
// Dagger cannot inject dependency into a private or local field
// For application context use the predefined qualifier @ApplicationContext and for activity context use @ActivityContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Its good to just create a single instance of Gson rather than creating multiple objects. Performance thing.
    @Inject lateinit var gson: Gson
    @Inject lateinit var networkStateListener: NetworkStateListener
    @Inject lateinit var handler: Handler

    private lateinit var viewModel: FavAnimeViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(FavAnimeViewModel::class.java)

        // Protection from config change. If data exists then dont call them. If however done explicitly through a button then obviously call
        if (null == viewModel.getAnimeList().value) loadAnimeListWrtNetworkChanges()
        if (null == viewModel.getAnime().value) loadAnimeWrtNetworkChanges()

        binding.btnAnimeList.setOnClickListener {
            loadAnimeListWrtNetworkChanges()
        }

        binding.btnAnime.setOnClickListener {
            loadAnimeWrtNetworkChanges()
        }

        setUpObservers()
    }

    private fun loadAnimeWrtNetworkChanges() {
        networkStateListener.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    viewModel.loadAnime("true")
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    showOfflineStrip()
                }
            }
        )
    }

    private fun loadAnimeListWrtNetworkChanges() {
        networkStateListener.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    viewModel.loadAnimeList()
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    showOfflineStrip()
                }
            }
        )
    }

    private fun showOfflineStrip() {
        binding.tvNetworkStateStrip.apply {
            text = context.getString(R.string.offline)
            visibility = View.VISIBLE
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }
        hideNetworkStripAfter5Sec()
    }

    private fun showOnlineStrip() {
        binding.tvNetworkStateStrip.apply {
            if (text == context.getString(R.string.online)) return@apply
            text = context.getString(R.string.online)
            visibility = View.VISIBLE
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark))
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }
        hideNetworkStripAfter5Sec()
    }

    private fun hideNetworkStripAfter5Sec() {
        handler.postDelayed({ binding.tvNetworkStateStrip.visibility = View.GONE }, 5000L)
    }

    private fun setUpObservers() {
        viewModel.getAnimeList().observe(this) { it: AnimeList? ->
            println("AnimeList chan: ${gson.toJson(it)}")
        }

        viewModel.getAnime().observe(this) { it: Anime? ->
            println("Anime chan: ${gson.toJson(it)}")
        }
    }
}
