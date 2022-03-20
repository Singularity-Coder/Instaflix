package com.singularitycoder.viewmodelstuff2.anime.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.Anime
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.FragmentAnimeDetailBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.network.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AnimeDetailFragment : BaseFragment() {

    companion object {
        fun newInstance() = AnimeDetailFragment()
    }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var networkState: NetworkState

    @Inject
    lateinit var utils: GeneralUtils

    @Inject
    lateinit var glide: RequestManager

    private lateinit var animeId: String
    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentAnimeDetailBinding

    private val animeViewModel: AnimeViewModel by viewModels()

    /** Non Null Activity and Context **/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAnimeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData()
        setUpDefaults()
        subscribeToObservers()
        setUpUserActionListeners()
        loadAnime()
    }

    private fun getIntentData() {
        animeId = arguments?.getString(IntentKey.ANIME_ID, "") ?: ""
        println("Anime Id received: $animeId")
    }

    private fun setUpDefaults() {
        // https://stackoverflow.com/questions/10713312/can-i-have-onscrolllistener-for-a-scrollview
        binding.scrollViewAnimeDetail.setOnScrollChangeListener { view: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            println("Scrolled to $scrollX, $scrollY from $oldScrollX, $oldScrollY")
            val bottomNav = nnActivity.findViewById<CardView>(R.id.card_bottom_nav)
            if (scrollY > 0) bottomNav.gone() else bottomNav.visible()
        }
    }

    private fun subscribeToObservers() {
        animeViewModel.getAnime().observe(viewLifecycleOwner) { it: ApiState<Anime?>? ->
            it ?: return@observe

            it onSuccess { data: Anime?, message: String ->
                if ("offline" == message) utils.showSnackBar(
                    view = binding.root,
                    message = getString(R.string.offline_try_again),
                    duration = Snackbar.LENGTH_LONG,
                    anchorView = activity?.findViewById(R.id.bottom_nav),
                    actionBtnText = this.getString(R.string.try_again)
                )
                utils.asyncLog(message = "Anime chan: %s", data)
                updateUI(anime = data)
            }

            it onFailure { data: Anime?, message: String ->
                utils.showToast(message = if ("NA" == message) getString(R.string.something_is_wrong) else message, context = nnContext)
            }

            it onLoading { loadingState: LoadingState ->
                when (loadingState) {
                    LoadingState.SHOW -> Unit
                    LoadingState.HIDE -> Unit
                }
            }
        }
    }

    private fun setUpUserActionListeners() {
        binding.ivContacts.onSafeClick {

        }

        binding.ivMessage.onSafeClick {

        }

        binding.ivWhatsapp.onSafeClick {

        }

        binding.ivShare.onSafeClick {

        }
    }

    private fun loadAnime() {
        if (null != animeViewModel.getAnime().value) return
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                animeViewModel.loadAnime(animeId)
            },
            offlineWork = {
                animeViewModel.loadAnime(animeId)
            }
        )
    }

    private fun updateUI(anime: Anime?) {
        binding.apply {
            glide.load(anime?.data?.coverImage).into(binding.ivCoverImage)
            glide.load(anime?.data?.bannerImage).into(binding.ivBannerImage)
            tvTitle.text = anime?.data?.titles?.en?.trimJunk() ?: getString(R.string.na)
            tvDesc.text = anime?.data?.descriptions?.en?.trimJunk() ?: getString(R.string.na)
            val rating = (anime?.data?.score?.div(10F))?.div(2F) ?: 0F
            println("Converted Rating: $rating vs Actual Rating: ${anime?.data?.score}")
            ratingAnimeDetail.rating = rating
            anime?.data?.genres?.forEach {
                val chip = Chip(nnContext).apply {
                    text = it
                    isCheckable = false
                    isClickable = false
                }

                binding.chipGroupGenre.addView(chip)
            }
        }
    }
}
