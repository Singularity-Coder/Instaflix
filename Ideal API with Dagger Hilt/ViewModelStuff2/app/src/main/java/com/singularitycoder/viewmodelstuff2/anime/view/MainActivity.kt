package com.singularitycoder.viewmodelstuff2.anime.view

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.aboutme.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.aboutme.viewmodel.AboutMeViewModel
import com.singularitycoder.viewmodelstuff2.anime.model.Anime
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeList
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.ActivityMainBinding
import com.singularitycoder.viewmodelstuff2.favorites.FavoritesFragment
import com.singularitycoder.viewmodelstuff2.more.MoreFragment
import com.singularitycoder.viewmodelstuff2.notifications.NotificationsFragment
import com.singularitycoder.viewmodelstuff2.utils.*
import com.singularitycoder.viewmodelstuff2.utils.network.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

// FIXME
// Choreographer: Skipped 34 frames! The application may be doing too much work on its main thread.

// TODO
// 1. Github graph ql api views
// 4. Tests
// 3. Basic list with custom views and multi views
// 5. Hilt Android doc - DI Activity Module & Qualifiers
// 6. ViewModels Android doc
// 7. LiveData Android doc - Mediator LiveData
// 8. Room Android doc - Room Foreign Key and Reationships
// 9. Work Manager
// 10. Foreground Service
// 11. Pagination
// Implement Timber
// Custom Views

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

    private val animeViewModel: AnimeViewModel by viewModels()
    private val aboutMeViewModel: AboutMeViewModel by viewModels()
//    val sharedViewModel: SharedViewModel by activityViewModels()  // Works only in Fragments

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBottomNav()
        loadData()
        setUpClickListeners()
        subscribeToObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkState.killNetworkCallback()
    }

    private fun setUpBottomNav() {
        fun show(fragment: Fragment): Boolean {
            // Home or Base fragments should not contain addToBackStack. But if u want to navigate to home frag then add HomeFrag
            supportFragmentManager.beginTransaction().replace(binding.bottomNavViewContainer.id, fragment).commit()
            return true
        }

        binding.bottomNav.setOnItemSelectedListener { it: MenuItem ->
            return@setOnItemSelectedListener when (it.itemId) {
                R.id.nav_home -> show(AnimeFragment())
                R.id.nav_favorites -> show(FavoritesFragment())
                R.id.nav_notifications -> show(NotificationsFragment())
                R.id.nav_more -> show(MoreFragment())
                else -> false
            }
        }
    }

    private fun loadData() {
        // Protection from config change. If data exists then dont call them. If however done explicitly through a button then obviously call
//        if (null == favAnimeViewModel.getAnimeList().value) loadAnimeList()
//        if (null == favAnimeViewModel.getAnime().value) loadAnime()
//        if (null == favAnimeViewModel.getFilteredAnimeList().value) loadFilteredAnimeList()
//        if (null == favAnimeViewModel.getRandomAnimeList().value) loadRandomAnimeList()
//        if (null == aboutMeViewModel.getAboutMe().value) loadAboutMe()
    }

    private fun setUpClickListeners() {
        binding.apply {
            btnGetAnimeList.setOnClickListener { loadAnimeList() }
            btnGetAnime.setOnClickListener { loadAnime() }
            btnGetFilteredAnimeList.setOnClickListener { loadFilteredAnimeList() }
            btnGetRandomAnimeList.setOnClickListener { loadRandomAnimeList() }
            btnAboutMe.setOnClickListener { loadAboutMe() }
        }
    }

    private fun loadAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    animeViewModel.loadAnimeList()
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    showOfflineStrip()
                    animeViewModel.loadAnimeList()
                }
            }
        )
    }

    private fun loadAnime() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    animeViewModel.loadAnime("true")
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    showOfflineStrip()
                    animeViewModel.loadAnime("true")
                }
            }
        )
    }

    private fun loadFilteredAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    animeViewModel.loadFilteredAnimeList(
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
                    showOfflineStrip()
                    animeViewModel.loadFilteredAnimeList(
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
            }
        )
    }

    private fun loadRandomAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    animeViewModel.loadRandomAnimeList()
                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    showOfflineStrip()
                    animeViewModel.loadRandomAnimeList()
                }
            }
        )
    }

    private fun loadAboutMe() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Main).launch {
                    showOnlineStrip()
                    aboutMeViewModel.loadAboutMe()

                }
            },
            offlineWork = {
                CoroutineScope(Main).launch {
                    showOfflineStrip()
                    aboutMeViewModel.loadAboutMe()
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
        animeViewModel.getAnimeList().observe(this) { it: ApiState<AnimeList?>? ->
            when (it) {
                is ApiState.Success -> {
                    if (getString(R.string.offline) == it.message) {
                        utils.showSnackBar(view = binding.root, message = getString(R.string.offline), duration = Snackbar.LENGTH_INDEFINITE, actionBtnText = this.getString(R.string.ok))
                    }
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

        animeViewModel.getAnime().observe(this) { it: ApiState<Anime?>? ->
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

        animeViewModel.getFilteredAnimeList().observe(this) { it: NetRes<AnimeList?>? ->
            it ?: return@observe
            when (it.status) {
                Status.SUCCESS -> {
                    if ("offline" == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.offline),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    if ("na" == it.message?.toLowCase() || getString(R.string.nothing_to_show) == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.nothing_to_show),
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

        animeViewModel.getRandomAnimeList().observe(this) { it: NetRes<AnimeList?>? ->
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
                Status.LOADING -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.progressCircular.visible()
                    LoadingState.HIDE -> binding.progressCircular.gone()
                }
                Status.ERROR -> utils.showToast(message = it.message ?: getString(R.string.something_is_wrong), context = this)
                else -> Unit
            }
        }

        aboutMeViewModel.getAboutMe().observe(this) { it: ApiState<GitHubProfileQueryModel?>? ->
            when (it) {
                is ApiState.Success -> {
                    if (getString(R.string.offline) == it.message) {
                        utils.showSnackBar(view = binding.root, message = getString(R.string.offline), duration = Snackbar.LENGTH_INDEFINITE, actionBtnText = this.getString(R.string.ok))
                    }
                    utils.asyncLog(message = "Github chan: %s", it.data)
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
    }
}
