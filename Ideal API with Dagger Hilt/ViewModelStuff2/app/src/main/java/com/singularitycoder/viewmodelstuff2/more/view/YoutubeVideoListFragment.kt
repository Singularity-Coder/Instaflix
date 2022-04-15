package com.singularitycoder.viewmodelstuff2.more.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.databinding.FragmentYoutubeVideoListBinding
import com.singularitycoder.viewmodelstuff2.more.model.YoutubeVideo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YoutubeVideoListFragment : BaseFragment() {

    companion object {
        fun newInstance(videoList: ArrayList<YoutubeVideo>) = YoutubeVideoListFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(VIDEO_LIST, videoList)
            }
        }
    }

    private var youtubeVideoList: List<YoutubeVideo> = listOf()
    private lateinit var binding: FragmentYoutubeVideoListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            youtubeVideoList = it.getParcelableArrayList<YoutubeVideo>(VIDEO_LIST) as ArrayList<YoutubeVideo>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentYoutubeVideoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpRecyclerView() {
        binding.rvVideoList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = YoutubeVideoListAdapter(
                youtubeVideoList = youtubeVideoList,
                youtubeVideoClickListener = { videoId: String ->

                }
            )
            setUpScrollListener()
        }
    }
}

private const val VIDEO_LIST = "VIDEO_LIST"

