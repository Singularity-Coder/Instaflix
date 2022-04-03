package com.singularitycoder.viewmodelstuff2.anime.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.like.LikeButton
import com.like.OnLikeListener
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.*
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.FragmentAnimeDetailBinding
import com.singularitycoder.viewmodelstuff2.favorites.Favorite
import com.singularitycoder.viewmodelstuff2.favorites.FavoritesViewModel
import com.singularitycoder.viewmodelstuff2.helpers.constants.DateType
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.checkThisOutList
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.network.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.helpers.utils.deviceHeight
import com.singularitycoder.viewmodelstuff2.helpers.utils.deviceWidth
import com.singularitycoder.viewmodelstuff2.helpers.utils.timeNow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.security.SecureRandom
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

    @Inject
    lateinit var secureRandom: SecureRandom

    var favoriteAnime: Favorite? = null

    private lateinit var animeId: String
    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentAnimeDetailBinding

    private val animeViewModel: AnimeViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels()

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
        setViewsBasedOnDeviceDimensions()
        binding.btnLike.isLiked = favoritesViewModel.getAnimeList().value?.any { it.id == favoriteAnime?.id } == true
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
                favoriteAnime = Favorite(
                    checkThisOut = checkThisOutList[secureRandom.nextInt(8)],
                    title = data?.data?.titles?.en?.trimJunk() ?: data?.data?.titles?.rj?.trimJunk(),
                    desc = data?.data?.descriptions?.en?.trimJunk() ?: data?.data?.descriptions?.jp?.trimJunk(),
                    score = data?.data?.score ?: 0,
                    coverImage = data?.data?.coverImage,
                    date = timeNow,
                    id = data?.data?.id ?: -1
                )
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
        binding.apply {
            ivContacts.onSafeClick {
                nnContext.shareSmsToAll()
            }
            ivSms.onSafeClick {
                nnContext.shareViaSms(phoneNum = "0000000000")
            }
            ivWhatsapp.onSafeClick {
                nnContext.shareViaWhatsApp(whatsAppPhoneNum = "0000000000")
            }
            ivShare.onSafeClick {
                nnActivity.shareViaApps(
                    imageDrawableOrUrl = nnContext.drawable(R.drawable.saitama),
                    imageView = binding.ivCoverImage,
                    title = "Fav Anime App",
                    subtitle = "Fav Anime App"
                )
            }
            ivEmail.onSafeClick {
                nnContext.shareViaEmail(email = "Friend's Email", subject = "Fav Anime App", desc = "Fav Anime App")
            }
        }

        binding.tvReadDesc.onSafeClick {

        }

        binding.tvLikeState.onSafeClick {
            binding.btnLike.performClick() // So when u click tvLikeState it inturn clicks btnLike which performs its action
        }

        // https://github.com/jd-alexander/LikeButton
        // https://www.studytonight.com/post/implement-twitter-heart-button-like-animation-in-android-app
        binding.btnLike.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton) {
                binding.tvLikeState.text = "Remove"
                favoritesViewModel.removeFromFavorites(favoriteAnime ?: return)
            }

            override fun unLiked(likeButton: LikeButton) {
                binding.tvLikeState.text = "Add to Favorites"
                favoritesViewModel.addToFavorites(favoriteAnime ?: return)
            }
        })

        // https://stackoverflow.com/questions/10713312/can-i-have-onscrolllistener-for-a-scrollview
        binding.scrollViewAnimeDetail.setOnScrollChangeListener { view: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            println("Scrolled to $scrollX, $scrollY from $oldScrollX, $oldScrollY")
            val bottomNav = nnActivity.findViewById<CardView>(R.id.card_bottom_nav)
            if (scrollY > 0) bottomNav.gone() else bottomNav.visible()
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
            if (anime?.data?.bannerImage.isNullOrBlankOrNaOrNullString()) {
                binding.ivBannerImage.gone()
                binding.cardCoverImage.setMargins(start = 16.dpToPx(), top = 16.dpToPx(), end = 0, bottom = 0)
            } else {
                glide.load(anime?.data?.bannerImage).into(binding.ivBannerImage)
            }
            tvTitle.text = anime?.data?.titles?.en?.trimJunk() ?: anime?.data?.titles?.rj?.trimJunk() ?: getString(R.string.na)
            tvDesc.text = anime?.data?.descriptions?.en?.trimJunk() ?: anime?.data?.descriptions?.jp?.trimJunk() ?: getString(R.string.na)
            val rating = (anime?.data?.score?.div(10F))?.div(2F) ?: 0F
            println("Converted Rating: $rating vs Actual Rating: ${anime?.data?.score}")
            ratingAnimeDetail.rating = rating

            tvStatus.text = String.format(
                getString(R.string.anime_detail_status_s),
                AnimeStatus.getText(anime?.data?.status?.toByte()) ?: getString(R.string.na)
            )
            tvFormat.text = String.format(
                getString(R.string.anime_detail_format_s),
                AnimeFormats.getText(anime?.data?.format?.toByte()) ?: getString(R.string.na)
            )
            tvStartDate.text = String.format(
                getString(R.string.anime_detail_start_date_s),
                anime?.data?.startDate?.utcTimeTo(DateType.dd_MMM_yyyy) ?: getString(R.string.na)
            )
            tvEndDate.text = String.format(
                getString(R.string.anime_detail_end_date_s),
                anime?.data?.endDate?.utcTimeTo(DateType.dd_MMM_yyyy) ?: getString(R.string.na)
            )
            tvSeasonPeriod.text = String.format(
                getString(R.string.anime_detail_season_period_s),
                AnimeSeasonPeriod.getText(anime?.data?.seasonPeriod?.toByte()) ?: getString(R.string.na)
            )
            tvSeasonYear.text = String.format(
                getString(R.string.anime_detail_season_year_s),
                anime?.data?.seasonYear ?: getString(R.string.na)
            )
            tvEpisodeCount.text = String.format(
                getString(R.string.anime_detail_episode_count_s),
                anime?.data?.episodesCount ?: getString(R.string.na)
            )
            tvEpisodeDuration.text = String.format(
                getString(R.string.anime_detail_episode_duration_s),
                anime?.data?.episodeDuration?.toString().plus(" minutes")
            )
            tvWeeklyAiringDate.text = String.format(
                getString(R.string.anime_detail_weekly_airing_date_s),
                AnimeWeeklyAiringDay.getText(anime?.data?.weeklyAiringDay?.toByte()) ?: getString(R.string.na)
            )

            anime?.data?.genres?.forEach {
                val chip = Chip(nnContext).apply {
                    text = it
                    isCheckable = false
                    isClickable = false
                    onSafeClick {
                        // call filter api with grid view
                    }
                }
                binding.chipGroupGenre.addView(chip)
            }
        }
    }

    // https://stackoverflow.com/questions/5042197/android-set-height-and-width-of-custom-view-programmatically
    private fun setViewsBasedOnDeviceDimensions() {
        binding.ivCoverImage.layoutParams.apply {
            width = deviceWidth() / 3
            height = deviceHeight() / 4
        }
        binding.ivBannerImage.layoutParams.height = (deviceHeight() / 3.5).toInt()
    }
}
