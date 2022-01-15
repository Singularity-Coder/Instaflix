package com.singularitycoder.viewmodelstuff2

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.databinding.ActivityMainBinding
import com.singularitycoder.viewmodelstuff2.model.Anime
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.utils.*
import com.singularitycoder.viewmodelstuff2.utils.network.ApiState
import com.singularitycoder.viewmodelstuff2.utils.network.LoadingState
import com.singularitycoder.viewmodelstuff2.utils.network.NetworkState
import com.singularitycoder.viewmodelstuff2.viewmodel.FavAnimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO
// 1. Call All APIs
// 2. Handle online offline funcs - DB calls
// 3. Basic list with custom views and multi views
// 4. Tests

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
    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var networkState: NetworkState

    @Inject
    lateinit var handler: Handler

    @Inject
    lateinit var utils: Utils

    val viewModel: FavAnimeViewModel by viewModels()
//    val sharedViewModel: SharedViewModel by activityViewModels()  // Works only in Fragments

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    override fun onDestroy() {
        super.onDestroy()
        networkState.killNetworkCallback()
    }

    private fun loadAnimeWrtNetworkChanges() {
        networkState.listenToNetworkChangesAndDoWork(
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
        networkState.listenToNetworkChangesAndDoWork(
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
            text = context.getString(R.string.offline).toUpCase()
            visibility = View.VISIBLE
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }
    }

    private fun showOnlineStrip() {
        binding.tvNetworkStateStrip.apply {
            if (text == context.getString(R.string.online).toUpCase()) return@apply
            text = context.getString(R.string.online).toUpCase()
            visibility = View.VISIBLE
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark))
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }
        hideNetworkStripAfter5Sec()
    }

    private fun hideNetworkStripAfter5Sec() {
        handler.postDelayed({ binding.tvNetworkStateStrip.visibility = View.GONE }, 5_000L)
    }

    private fun setUpObservers() {
        viewModel.getAnimeList().observe(this) { it: ApiState<AnimeList?>? ->
            when (it) {
                is ApiState.Success -> {
                    utils.asyncLog(message = "AnimeList chan: %s", it.data)
                }
                is ApiState.Loading -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.progressCircular.visible()
                    LoadingState.HIDE -> binding.progressCircular.gone()
                }
                is ApiState.Error -> {
                    utils.showSnackBar(view = binding.root, message = it.message, duration = Snackbar.LENGTH_INDEFINITE, actionBtnText = this.getString(R.string.ok))
                    binding.progressCircular.gone()
                }
                null -> Unit
            }
        }

        viewModel.getAnime().observe(this) { it: ApiState<Anime?>? ->
            it ?: return@observe
            when (it) {
                is ApiState.Success -> {
                    if ("offline" == it.message) showToast(getString(R.string.offline))
                    utils.asyncLog(message = "Anime chan: %s", it)
                }
                is ApiState.Loading -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.progressCircular.visible()
                    LoadingState.HIDE -> binding.progressCircular.gone()
                }
                is ApiState.Error -> {
                    utils.showSnackBar(
                        view = binding.root,
                        message = if ("NA" == it.message) getString(R.string.something_is_wrong) else it.message,
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    binding.progressCircular.gone()
                }
            }
        }
    }
}
