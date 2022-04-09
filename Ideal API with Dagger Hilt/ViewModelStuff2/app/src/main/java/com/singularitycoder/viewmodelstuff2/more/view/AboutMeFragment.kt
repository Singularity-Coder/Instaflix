@file:OptIn(ExperimentalCoroutinesApi::class)

package com.singularitycoder.viewmodelstuff2.more.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.FragmentAboutMeBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.animeFightsList
import com.singularitycoder.viewmodelstuff2.helpers.constants.animeMusicList
import com.singularitycoder.viewmodelstuff2.helpers.constants.animeSakugaList
import com.singularitycoder.viewmodelstuff2.helpers.constants.otherMusicList
import com.singularitycoder.viewmodelstuff2.helpers.extensions.changeColor
import com.singularitycoder.viewmodelstuff2.helpers.extensions.color
import com.singularitycoder.viewmodelstuff2.helpers.extensions.drawable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.abs

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
class AboutMeFragment : Fragment() {

    companion object {
        /** Use this factory method to create a new instance of this fragment using the provided parameters. */
        @JvmStatic
        fun newInstance(param1: String, param2: String) = AboutMeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }

        @JvmStatic
        fun newInstance() = AboutMeFragment()
    }

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentAboutMeBinding

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
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAboutMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAppBarLayout()
        setUpToolbar()
        setUpViewPager()
    }

    private fun setUpAppBarLayout() {
        binding.appbarAbout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout1: AppBarLayout, verticalOffset: Int ->
            if (abs(verticalOffset) - appBarLayout1.totalScrollRange == 0) {
                // COLLAPSED STATE
                binding.toolbarAbout.apply {
                    setBackgroundColor(nnContext.color(R.color.purple_500))
                    setTitleTextColor(nnContext.color(R.color.white))
                }
//                binding.tabLayoutAbout.apply {
//                    setBackgroundColor(nnContext.color(R.color.white))
//                    setTabTextColors(nnContext.color(R.color.white_70), nnContext.color(R.color.white))
//                }
                nnActivity.supportActionBar?.setHomeAsUpIndicator(nnContext.drawable(R.drawable.ic_baseline_arrow_back_24)?.changeColor(nnContext, R.color.white))
            } else {
                // EXPANDED STATE
                binding.toolbarAbout.apply {
                    setBackgroundColor(Color.TRANSPARENT)
                    setTitleTextColor(nnContext.color(android.R.color.transparent))
                }
//                binding.tabLayoutAbout.apply {
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setTabTextColors(nnContext.color(R.color.purple_500), nnContext.color(R.color.purple_500))
//                }
                nnActivity.supportActionBar?.setHomeAsUpIndicator(nnContext.drawable(R.drawable.ic_baseline_arrow_back_24)?.changeColor(nnContext, R.color.white))
            }
        })
    }

    private fun setUpToolbar() {
        nnActivity.setSupportActionBar(binding.toolbarAbout)
        if (nnActivity.supportActionBar != null) {
            nnActivity.supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "About Me"
            }
        }
    }

    // Fav Anime Memes, Fav Anime Walls
    private fun setUpViewPager() {
        binding.viewpagerAbout.adapter = YoutubeVideoViewPagerAdapter(fragmentManager = nnActivity.supportFragmentManager, lifecycle = lifecycle)
        TabLayoutMediator(binding.tabLayoutAbout, binding.viewpagerAbout) { tab, position ->
            tab.text = when (position) {
                0 -> aboutMeTabsList[0]
                1 -> aboutMeTabsList[1]
                2 -> aboutMeTabsList[2]
                else -> aboutMeTabsList[3]
            }
        }.attach()
    }
}

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

val aboutMeTabsList = listOf(
    "Fav Anime Fights",
    "Fav Anime Music",
    "Fav Music",
    "Fav Anime Sakuga"
)

class YoutubeVideoViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = aboutMeTabsList.size
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> YoutubeVideoListFragment.newInstance(ArrayList(animeFightsList))
        1 -> YoutubeVideoListFragment.newInstance(ArrayList(animeMusicList))
        2 -> YoutubeVideoListFragment.newInstance(ArrayList(otherMusicList))
        else -> YoutubeVideoListFragment.newInstance(ArrayList(animeSakugaList))
    }
}

