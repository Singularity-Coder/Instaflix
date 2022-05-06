package com.singularitycoder.viewmodelstuff2.more.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.FragmentAboutMeBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.*
import com.singularitycoder.viewmodelstuff2.helpers.extensions.visible
import com.singularitycoder.viewmodelstuff2.helpers.network.ApiState
import com.singularitycoder.viewmodelstuff2.helpers.network.LoadingState
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.more.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.more.viewmodel.MoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [AboutMeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

// https://developers.google.com/youtube/android/player
// https://guides.codepath.com/android/Streaming-Youtube-Videos-with-YouTubePlayerView
// https://www.sitepoint.com/using-the-youtube-api-to-embed-video-in-an-android-app/
// https://stackoverflow.com/questions/18175397/add-youtube-data-api-to-android-studio
// https://developers.google.com/youtube/v3/docs/playlists/list

// Replace manual lists with Youtube Playlists API. Or get everything from firebase

@AndroidEntryPoint
class AboutMeFragment : BaseFragment() {

    companion object {
        /** Use this factory method to create a new instance of this fragment using the provided parameters. */
        @JvmStatic
        fun newInstance(param1: String) = AboutMeFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM1, param1) }
        }

        @JvmStatic
        fun newInstance() = AboutMeFragment()
    }

    private var param1: String? = null

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentAboutMeBinding

    private val moreViewModel: MoreViewModel by viewModels()

    @Inject
    lateinit var networkState: NetworkState

    @Inject
    lateinit var utils: GeneralUtils

    @Inject
    lateinit var glide: RequestManager

    /** NonNull Context & Activity **/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAboutMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
        subscribeToObservers()
        loadData()
        setUpUserActionListeners()
    }

    private fun setUpViewPager() {
        binding.viewpagerAbout.adapter = YoutubeVideoViewPagerAdapter(fragmentManager = nnActivity.supportFragmentManager, lifecycle = lifecycle)
        TabLayoutMediator(binding.tabLayoutAbout, binding.viewpagerAbout) { tab, position ->
            tab.text = when (position) {
                0 -> aboutMeTabNamesList[0]
                1 -> aboutMeTabNamesList[1]
                2 -> aboutMeTabNamesList[2]
                3 -> aboutMeTabNamesList[3]
                4 -> aboutMeTabNamesList[4]
                else -> ""
            }
        }.attach()
    }

    private fun subscribeToObservers() {
        moreViewModel.getAboutMe().observe(viewLifecycleOwner) { it: ApiState<GitHubProfileQueryModel?>? ->
            when (it) {
                is ApiState.Success -> {
                    if (getString(R.string.offline) == it.message) {
                        utils.showSnackBar(view = binding.root, message = getString(R.string.offline), duration = Snackbar.LENGTH_LONG, actionBtnText = this.getString(R.string.ok))
                    }
                    utils.asyncLog(message = "Github chan: %s", it.data)
                    glide.load(it.data?.data?.repositoryOwner?.avatarUrl).into(binding.ivProfilePic)
                    binding.tvName.text = it.data?.data?.repositoryOwner?.name ?: getString(R.string.na)
                    binding.tvProfileDesc.text = "Github: @${it.data?.data?.repositoryOwner?.login ?: getString(R.string.na)}"
                }
                is ApiState.Loading -> when (it.loadingState) {
                    LoadingState.SHOW -> Unit
                    LoadingState.HIDE -> Unit
                }
                is ApiState.Error -> {
                    utils.showToast(message = it.message, context = nnContext)
                }
                null -> Unit
            }
        }
    }

    private fun loadData() {
        /*if (null == moreViewModel.getAboutMe().value) */loadAboutMe()
    }

    private fun setUpUserActionListeners() {
        nnActivity.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nnActivity.findViewById<CardView>(R.id.card_bottom_nav).visible()
                nnActivity.supportFragmentManager.popBackStackImmediate()
            }
        })
    }

    private fun loadAboutMe() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                moreViewModel.loadAboutMe()
            },
            offlineWork = {
                // Crashing for some reason due to Gson
            }
        )
    }
}

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

class YoutubeVideoViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = aboutMeTabNamesList.size
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> YoutubeVideoListFragment.newInstance(ArrayList(animeFightsList), aboutMeTabNamesList[0])
        1 -> YoutubeVideoListFragment.newInstance(ArrayList(animeMusicList), aboutMeTabNamesList[1])
        2 -> YoutubeVideoListFragment.newInstance(ArrayList(epicAnimeMomentsList), aboutMeTabNamesList[2])
        3 -> YoutubeVideoListFragment.newInstance(ArrayList(otherEpicMomentsList), aboutMeTabNamesList[3])
        else -> YoutubeVideoListFragment.newInstance(ArrayList(otherMusicList), aboutMeTabNamesList[4])
    }
}

