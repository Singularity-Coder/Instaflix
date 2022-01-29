package com.singularitycoder.viewmodelstuff2.more

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.view.MainActivity
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel

class MoreFragment : Fragment() {

    companion object {
        fun newInstance() = MoreFragment()
    }

    val viewModel: AnimeViewModel by viewModels()
    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fav_anime_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // This is still useful in some cases over onResume.
        // Basically called when this fragment is visible to user or user can see this only when this fragment is in background and comes to foreground.
        // So this wont get triggered when you launch it. Only when you come back from another fragment.
    }
}
