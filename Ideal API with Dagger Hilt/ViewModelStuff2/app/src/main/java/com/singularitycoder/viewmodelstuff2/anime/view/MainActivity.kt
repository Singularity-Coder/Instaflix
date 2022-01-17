package com.singularitycoder.viewmodelstuff2.anime.view

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.ActivityMainBinding
import com.singularitycoder.viewmodelstuff2.anime.model.Anime
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeList
import com.singularitycoder.viewmodelstuff2.utils.*
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.FavAnimeViewModel
import com.singularitycoder.viewmodelstuff2.utils.network.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

// FIXME
// Choreographer: Skipped 34 frames! The application may be doing too much work on its main thread.
// For some reason sequential livedata.postValue() is not working. So loading has to be commented out

// TODO
// 1. Github graph ql api
// 3. Basic list with custom views and multi views
// 4. Tests
// 5. Hilt Android doc
// 6. ViewModels Android doc
// 7. LiveData Android doc
// 8. Work Manager
// 9. Foreground Service
// 10. Pagination

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

// Field injection is allowed in Activities and Fragments but in normal classes only constructor injection is allowed

// Lazy intialisation in Hilt - @Inject lateinit var gson: Lazy<Gson> - and the access it using gson.value. But its not working - https://stackoverflow.com/questions/51127524/dagger-lazy-during-constructor-injection

@ExperimentalCoroutinesApi
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
        loadData()
        setUpClickListeners()
        subscribeToObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkState.killNetworkCallback()
    }

    private fun loadData() {
        // Protection from config change. If data exists then dont call them. If however done explicitly through a button then obviously call
        if (null == viewModel.getAnimeList().value) loadAnimeList()
        if (null == viewModel.getAnime().value) loadAnime()
        if (null == viewModel.getFilteredAnimeList().value) loadFilteredAnimeList()
        if (null == viewModel.getRandomAnimeList().value) loadRandomAnimeList()
    }

    private fun setUpClickListeners() {
        binding.btnGetAnimeList.setOnClickListener { loadAnimeList() }
        binding.btnGetAnime.setOnClickListener { loadAnime() }
        binding.btnGetFilteredAnimeList.setOnClickListener { loadFilteredAnimeList() }
        binding.btnGetRandomAnimeList.setOnClickListener { loadRandomAnimeList() }
    }

    private fun loadAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    viewModel.loadAnimeList()
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    viewModel.loadAnimeList()
                    showOfflineStrip()
                }
            }
        )
    }

    private fun loadAnime() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    viewModel.loadAnime("true")
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    viewModel.loadAnime("true")
                    showOfflineStrip()
                }
            }
        )
    }

    private fun loadFilteredAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    viewModel.loadFilteredAnimeList(
                        title = "Code Geass",
                        aniListId = null,
                        malId = null,
                        formats = null,
                        status = null,
                        year = null,
                        season = null,
                        genres = null,
                        nsfw = false
                    )
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    viewModel.loadFilteredAnimeList(
                        title = "Code Geass",
                        aniListId = null,
                        malId = null,
                        formats = null,
                        status = null,
                        year = null,
                        season = null,
                        genres = null,
                        nsfw = false
                    )
                    showOfflineStrip()
                }
            }
        )
    }

    private fun loadRandomAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    viewModel.loadRandomAnimeList()
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    viewModel.loadRandomAnimeList()
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

    private fun subscribeToObservers() {
        viewModel.getAnimeList().observe(this) { it: ApiState<AnimeList?>? ->
            when (it) {
                is ApiState.Success -> {
                    if (getString(R.string.offline) == it.message) utils.showSnackBar(view = binding.root, message = getString(R.string.offline), duration = Snackbar.LENGTH_INDEFINITE, actionBtnText = this.getString(R.string.ok))
                    utils.asyncLog(message = "AnimeList chan: %s", it.data)
                }
                is ApiState.Loading -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.progressCircular.visible()
                    LoadingState.HIDE -> binding.progressCircular.gone()
                }
                is ApiState.Error -> {
                    binding.progressCircular.gone()
                    utils.showToast(message = it.message, context = this)
                }
                null -> Unit
            }
        }

        viewModel.getAnime().observe(this) { it: ApiState<Anime?>? ->
            it ?: return@observe
            when (it) {
                is ApiState.Success -> {
                    if ("offline" == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.offline),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    utils.asyncLog(message = "Anime chan: %s", it.data)
                }
                is ApiState.Loading -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.progressCircular.visible()
                    LoadingState.HIDE -> binding.progressCircular.gone()
                }
                is ApiState.Error -> {
                    binding.progressCircular.gone()
                    utils.showToast(message = if ("NA" == it.message) getString(R.string.something_is_wrong) else it.message, context = this)
                }
            }
        }

        viewModel.getFilteredAnimeList().observe(this) { it: NetRes<AnimeList?>? ->
            it ?: return@observe
            when (it.status) {
                Status.SUCCESS -> {
                    if ("offline" == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.offline),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    if ("na" == it.message?.toLowCase() || getString(R.string.empty_response) == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.empty_response),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    utils.asyncLog(message = "Filtered Anime chan: %s", it.data)
                }
                Status.LOADING -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.progressCircular.visible()
                    LoadingState.HIDE -> binding.progressCircular.gone()
                }
                Status.ERROR -> {
                    binding.progressCircular.gone()
                    utils.showToast(message = it.message ?: getString(R.string.something_is_wrong), context = this)
                    println(it.message ?: getString(R.string.something_is_wrong))
                }
            }
        }

        viewModel.getRandomAnimeList().observe(this) { it: NetRes<AnimeList?>? ->
            when (it?.status) {
                Status.SUCCESS -> {
                    if (getString(R.string.offline) == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.offline),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    utils.asyncLog(message = "Random Anime chan: %s", it.data)
                }
                Status.LOADING -> Unit
                Status.ERROR -> {
                    binding.progressCircular.gone()
                    utils.showToast(message = it.message ?: getString(R.string.something_is_wrong), context = this)
                }
                else -> Unit
            }
        }
    }
}
