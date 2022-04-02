package com.singularitycoder.viewmodelstuff2.anime.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeList
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.FragmentHomeBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.network.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    @Inject
    lateinit var homeAdapter: HomeAdapter

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var networkState: NetworkState

    @Inject
    lateinit var utils: GeneralUtils

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentHomeBinding

    private val duplicateAnimeDataList = ArrayList<AnimeData>()
    private val duplicateAnimeSearchDataList = ArrayList<AnimeData>()

    private val animeViewModel: AnimeViewModel by viewModels()

    /** NonNull Context & Activity **/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDefaults()
        setUpRecyclerView()
        subscribeToObservers()
        setUpUserActionListeners()
        loadData()
    }

    private fun setUpDefaults() {
        binding.swipeRefreshHome.setColorSchemeColors(
            nnContext.color(R.color.purple_500),
            nnContext.color(R.color.purple_700)
        )
        binding.customSearch.getSearchView().disable()
//        binding.customSearch.getSearchView().revealBottomToTop()
//        nnActivity.findViewById<BottomNavigationView>(R.id.bottom_nav).revealTopToBottom()
    }

    private fun setUpRecyclerView() {
        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(nnContext)
            adapter = homeAdapter
            setUpScrollListener { recyclerView, dx, dy ->
                Timber.i("dx: $dx, dy: $dy")
                if (dy > 0) binding.customSearch.gone() else binding.customSearch.visible()
            }
        }
    }

    private fun subscribeToObservers() {
        animeViewModel.getAnimeList().observe(viewLifecycleOwner) { it: ApiState<AnimeList?>? ->
            binding.swipeRefreshHome.isRefreshing = false
            when (it) {
                is ApiState.Success -> {
                    if (getString(R.string.offline) == it.message) {
                        utils.showSnackBar(
                            view = binding.root,
                            message = getString(R.string.offline_try_again),
                            duration = Snackbar.LENGTH_INDEFINITE,
                            anchorView = activity?.findViewById(R.id.bottom_nav),
                            actionBtnText = this.getString(R.string.try_again)
                        ) { loadAnimeList() }
                    }
                    utils.asyncLog(message = "AnimeList chan: %s", it.data)
//                    val animeDataCopy = (it.data as AnimeList).data.documents[0].copy() // This is equivalent to clone(). Shallow copy
                    duplicateAnimeDataList.apply {
                        clear()
                        addAll(it.data?.data?.documents ?: listOf())
                    }
                    updateHomeAnimeList(list = duplicateAnimeDataList)
                    binding.customSearch.getSearchView().enable()
                }
                is ApiState.Loading -> when (it.loadingState) {
                    LoadingState.SHOW -> binding.layoutShimmerHomeLoader.shimmerLoader.visible()
                    LoadingState.HIDE -> binding.layoutShimmerHomeLoader.shimmerLoader.gone()
                }
                is ApiState.Error -> {
                    binding.layoutShimmerHomeLoader.shimmerLoader.gone()
                    binding.customSearch.getSearchView().enable()
                    utils.showToast(message = it.message, context = nnContext)
                }
                null -> Unit
            }
        }

        animeViewModel.getFilteredAnimeList().observe(viewLifecycleOwner) { it: NetRes<AnimeList?>? ->
            it ?: return@observe
            binding.swipeRefreshHome.isRefreshing = false
            when (it.status) {
                Status.SUCCESS -> {
                    if ("offline" == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.offline_try_again),
                        duration = Snackbar.LENGTH_LONG,
                        anchorView = activity?.findViewById(R.id.bottom_nav),
                        actionBtnText = this.getString(R.string.ok)
                    )
                    if ("na" == it.message?.toLowCase() || getString(R.string.nothing_to_show) == it.message) utils.showSnackBar(
                        view = binding.root,
                        message = getString(R.string.nothing_to_show),
                        duration = Snackbar.LENGTH_LONG,
                        anchorView = activity?.findViewById(R.id.bottom_nav),
                        actionBtnText = this.getString(R.string.ok)
                    )
                    duplicateAnimeSearchDataList.apply {
                        clear()
                        addAll(it.data?.data?.documents ?: emptyList())
                    }
                    updateHomeAnimeList(list = duplicateAnimeSearchDataList, type = "search")
                    utils.asyncLog(message = "Filtered Anime chan: %s", it.data)
                }
                Status.LOADING -> when (it.loadingState) {
                    LoadingState.SHOW -> {
                        binding.rvHome.gone()
                        binding.layoutShimmerHomeLoader.shimmerLoader.visible()
                    }
                    LoadingState.HIDE -> {
                        binding.rvHome.visible()
                        binding.layoutShimmerHomeLoader.shimmerLoader.gone()
                    }
                }
                Status.ERROR -> {
                    binding.layoutShimmerHomeLoader.shimmerLoader.gone()
                    utils.showToast(message = it.message ?: getString(R.string.something_is_wrong), context = nnContext)
                    println(it.message ?: getString(R.string.something_is_wrong))
                }
            }
        }
    }

    private fun setUpUserActionListeners() {
        binding.customSearch.getSearchView().addTextChangedListener { it: Editable? ->
            if (it?.isBlank() == true) {
                updateHomeAnimeList(list = duplicateAnimeDataList.toList())
            } else {
                loadFilteredAnimeList(title = it.toString())
            }
        }

        homeAdapter.setSpotlightViewClickListener { animeId: String ->
            nnActivity.showAnimeDetailsOfThis(animeId)
        }

        homeAdapter.setStandardViewClickListener { animeId: String ->
            nnActivity.showAnimeDetailsOfThis(animeId)
        }

        binding.swipeRefreshHome.setOnRefreshListener { loadAnimeList() }
    }

    private fun loadData() {
        // Protection from config change. If data exists then dont call them. If however done explicitly through a button then obviously call
        if (null == animeViewModel.getAnimeList().value) loadAnimeList()
    }

    private fun loadAnimeList() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                println("Class Name: ${this.javaClass.simpleName}, Thread Name: ${Thread.currentThread().name}")
                animeViewModel.loadAnimeList()
            },
            offlineWork = {
                animeViewModel.loadAnimeList()
            }
        )
    }

    private fun loadFilteredAnimeList(title: String) {
        fun loadData() {
            animeViewModel.loadFilteredAnimeList(
                title = title,
                aniListId = null,
                malId = null,
                formats = null,
                status = null,
                year = null,
                seasonPeriod = null,
                genres = null,
                nsfw = false
            )
        }

        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = { loadData() },
            offlineWork = { loadData() }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateHomeAnimeList(list: List<AnimeData>, type: String = "") {
        homeAdapter.apply {
            homeList = list
            notifyDataSetChanged()
        }
        if (type == "search") {
            if (list.isEmpty()) {
                binding.lottieEmptyList.gone()
                binding.lottieEmptySearch.visible()
            } else {
                binding.lottieEmptySearch.gone()
            }
        } else {
            if (list.isEmpty()) {
                binding.lottieEmptySearch.gone()
                binding.lottieEmptyList.visible()
            } else {
                binding.lottieEmptyList.gone()
            }
        }
    }
}
