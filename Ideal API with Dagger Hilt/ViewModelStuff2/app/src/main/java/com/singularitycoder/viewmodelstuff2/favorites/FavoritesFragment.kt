package com.singularitycoder.viewmodelstuff2.favorites

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.databinding.FragmentFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Voice Search
// Search Filters

@AndroidEntryPoint
class FavoritesFragment : BaseFragment() {

    companion object {
        fun newInstance() = FavoritesFragment()
    }

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
    }

    private fun setUpDefaults() {
        favoritesViewModel.getAnimeList()
    }

    private fun setUpRecyclerView() {
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(nnContext)
            adapter = favoritesAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeToObservers() {
        favoritesViewModel.getAnimeList().observe(viewLifecycleOwner) { it: List<Favorite>? ->
            it ?: return@observe
            favoritesAdapter.favoritesList = it
            binding.rvFavorites.adapter?.notifyDataSetChanged()
        }
    }
}
