package com.singularitycoder.viewmodelstuff2.notifications.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.anime.model.RandomAnimeListData
import com.singularitycoder.viewmodelstuff2.databinding.FragmentNotificationsBinding
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.notifications.viewmodel.NotificationsViewModel
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.WorkerTag
import com.singularitycoder.viewmodelstuff2.helpers.constants.checkThisOutList
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.network.LoadingState
import com.singularitycoder.viewmodelstuff2.helpers.network.NetRes
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.network.Status
import com.singularitycoder.viewmodelstuff2.helpers.service.AnimeRecommendationWorker
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Workmanager 15 min random anime recom - control it to launch it just once
// Reverese the anime list to reverse chron order
// Date must be latest date
// Launch notification
// On click of notification launch main activity and paas it to detail fragment
// Foreground service

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotificationsFragment : BaseFragment() {

    companion object {
        fun newInstance() = NotificationsFragment()
    }

    @Inject
    lateinit var notificationsAdapter: NotificationsAdapter

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var networkState: NetworkState

    @Inject
    lateinit var utils: GeneralUtils

    @Inject
    lateinit var secureRandom: SecureRandom

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentNotificationsBinding

    private val notificationsViewModel: NotificationsViewModel by viewModels()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(IntentKey.DATA_LOAD_RANDOM_ANIME, false)) {
                goAsync { loadRandomAnimeList() }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        subscribeToObservers()
        loadRandomAnimeList()
        nnContext.startAnimeRecommendationWorker()
    }

    override fun onResume() {
        super.onResume()
        // Register local broadcast
        LocalBroadcastManager.getInstance(nnContext).registerReceiver(receiver, IntentFilter(IntentKey.ACTION_SHAKE))
    }

    override fun onPause() {
        super.onPause()
        // Unregister local broadcast
        LocalBroadcastManager.getInstance(nnContext).unregisterReceiver(receiver)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // This is still useful in some cases over onResume.
        // Basically called when this fragment is visible to user or user can see this only when this fragment is in background and comes to foreground.
        // So this wont get triggered when you launch it. Only when you come back from another fragment.
        // This is not a replacement for onResume as this loads too quickly and is limited in use. We can intentionally delay though.
    }

    private fun setUpRecyclerView() {
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(nnContext).apply {
                reverseLayout = true
                stackFromEnd = true // Latest items at the top
            }
            adapter = notificationsAdapter
            setUpScrollListener()
        }
    }

    private fun loadRandomAnime() {
        if (null == notificationsViewModel.getRandomAnimeListFromApi()?.value) loadRandomAnimeList()
    }

    private fun loadRandomAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.tvNetworkState.showOnlineStrip()
                    notificationsViewModel.loadRandomAnimeListFromDb()
                }
            },
            offlineWork = {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.tvNetworkState.showOfflineStrip()
                    notificationsViewModel.loadRandomAnimeListFromDb()
                }
            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeToObservers() {
        notificationsViewModel.getRandomAnimeListFromApi()?.observe(viewLifecycleOwner) { it: NetRes<RandomAnimeListData?>? ->
            when (it?.status) {
                Status.SUCCESS -> {
                    if (getString(R.string.offline) == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.offline),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionBtnText = this.getString(R.string.ok)
                    )
                    utils.asyncLog(message = "Random Anime chan: %s", it.data)
                    notificationsAdapter.notificationsList = it.data?.data?.map { animeData: AnimeData ->
                        Notification(
                            aniListId = animeData.aniListId,
                            checkThisOut = checkThisOutList[secureRandom.nextInt(8)],
                            title = animeData.titles.en,
                            score = animeData.score,
                            coverImage = animeData.coverImage
                        )
                    } ?: emptyList()
                    notificationsAdapter.notifyDataSetChanged()
                }
                Status.LOADING -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.layoutShimmerNotificationsLoader.shimmerLoader.visible()
                    LoadingState.HIDE -> binding.layoutShimmerNotificationsLoader.shimmerLoader.gone()
                }
                Status.ERROR -> {
                    utils.showToast(message = it.message ?: getString(R.string.something_is_wrong), context = nnContext)
                    binding.layoutShimmerNotificationsLoader.shimmerLoader.gone()
                }
                else -> Unit
            }
        }

        notificationsViewModel.getRandomAnimeListFromDb().observe(viewLifecycleOwner) { it: NetRes<List<Notification>>? ->
            when (it?.status) {
                Status.SUCCESS -> {
                    utils.asyncLog(message = "Random Anime chan: %s", it.data)
                    notificationsAdapter.notificationsList = it.data ?: emptyList()
                    notificationsAdapter.notifyDataSetChanged()
                }
                Status.LOADING -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.layoutShimmerNotificationsLoader.shimmerLoader.visible()
                    LoadingState.HIDE -> binding.layoutShimmerNotificationsLoader.shimmerLoader.gone()
                }
                Status.ERROR -> {
                    utils.showToast(message = it.message ?: getString(R.string.something_is_wrong), context = nnContext)
                    binding.layoutShimmerNotificationsLoader.shimmerLoader.gone()
                }
                else -> Unit
            }
        }
    }
}
