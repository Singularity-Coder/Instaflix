package com.singularitycoder.viewmodelstuff2.favorites

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.ViewTarget
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.databinding.FragmentFavoritesBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.wait
import com.singularitycoder.viewmodelstuff2.helpers.utils.waitFor
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

// Voice Search
// Search Filters

@AndroidEntryPoint
class FavoritesFragment : BaseFragment() {

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var favoritesAdapter: FavoritesAdapter

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentFavoritesBinding

    private val favoritesViewModel: FavoritesViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDefaults()
        setUpRecyclerView()
        subscribeToObservers()
        setUpUserActionListeners()
    }

    private fun setUpDefaults() {
        binding.swipeRefreshFavorites.setDefaultColors(nnContext)
        favoritesViewModel.getFavoritesList()
    }

    private fun setUpRecyclerView() {
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(nnContext)
            adapter = favoritesAdapter
            setUpScrollListener()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeToObservers() {
        favoritesViewModel.getFavoritesList().observe(viewLifecycleOwner) { it: List<Favorite>? ->
            it ?: return@observe
            favoritesAdapter.favoritesList = it.sortedBy { it.date }.reversed()
            binding.rvFavorites.adapter?.notifyDataSetChanged()
            binding.swipeRefreshFavorites.isRefreshing = false
        }
    }

    private fun setUpUserActionListeners() {
        (binding.rvFavorites.adapter as FavoritesAdapter).setFavoriteViewClickListener { animeId: String ->
            nnActivity.showAnimeDetailsOfThis(animeId)
        }

        binding.swipeRefreshFavorites.setOnRefreshListener {
            favoritesViewModel.getFavoritesList()
            binding.swipeRefreshFavorites.isRefreshing = false
        }
    }
}
