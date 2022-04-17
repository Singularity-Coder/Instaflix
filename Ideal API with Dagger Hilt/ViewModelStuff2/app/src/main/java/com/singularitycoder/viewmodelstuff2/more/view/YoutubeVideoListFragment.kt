package com.singularitycoder.viewmodelstuff2.more.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.databinding.FragmentYoutubeVideoListBinding
import com.singularitycoder.viewmodelstuff2.more.model.YoutubeVideo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YoutubeVideoListFragment : BaseFragment() {

    companion object {
        fun newInstance(
            videoList: ArrayList<YoutubeVideo>,
            listType: String
        ) = YoutubeVideoListFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(KEY_YOUTUBE_VIDEO_LIST, videoList)
                putString(KEY_YOUTUBE_LIST_TYPE, listType)
            }
        }
    }

    private var youtubeVideoList: List<YoutubeVideo> = listOf()
    private var youtubeVideoListType: String = ""

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentYoutubeVideoListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            youtubeVideoList = it.getParcelableArrayList<YoutubeVideo>(KEY_YOUTUBE_VIDEO_LIST) as ArrayList<YoutubeVideo>
            youtubeVideoListType = it.getString(KEY_YOUTUBE_LIST_TYPE, "")
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
                    val intent = Intent(nnActivity, YoutubeVideoActivity::class.java).apply {
                        putExtra(KEY_YOUTUBE_VIDEO_ID, videoId)
                        putExtra(KEY_YOUTUBE_LIST_TYPE, youtubeVideoListType)
                    }
                    startActivity(intent)
                }
            )
            setUpScrollListener()
        }
    }
}

private const val KEY_YOUTUBE_VIDEO_LIST = "KEY_YOUTUBE_VIDEO_LIST"
const val KEY_YOUTUBE_LIST_TYPE = "KEY_YOUTUBE_LIST_TYPE"
const val KEY_YOUTUBE_VIDEO_ID = "KEY_YOUTUBE_VIDEO_ID"


