package com.singularitycoder.viewmodelstuff2.anime.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.like.LikeButton
import com.like.OnLikeListener
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.dao.AnimeDao
import com.singularitycoder.viewmodelstuff2.anime.model.*
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.DialogGeneratedBarcodeBinding
import com.singularitycoder.viewmodelstuff2.databinding.FragmentAnimeDetailBinding
import com.singularitycoder.viewmodelstuff2.favorites.Favorite
import com.singularitycoder.viewmodelstuff2.favorites.FavoritesViewModel
import com.singularitycoder.viewmodelstuff2.helpers.constants.DateType
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.checkThisOutList
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.network.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.*
import com.singularitycoder.viewmodelstuff2.more.view.KEY_YOUTUBE_VIDEO_ID
import com.singularitycoder.viewmodelstuff2.more.view.YoutubeVideoActivity
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.SecureRandom
import java.util.*
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

    @Inject
    lateinit var animeDao: AnimeDao

    @Inject
    lateinit var recommendationsAdapter: RecommendationsAdapter

    @Inject
    lateinit var episodesAdapter: EpisodesAdapter

    private var favoriteAnime: Favorite? = null
    private var textToSpeech: TextToSpeech? = null
    private var animeFromApi: Anime? = null

    private lateinit var idOfAnime: String
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
        setUpRecyclerView()
        subscribeToObservers()
        setUpUserActionListeners()
        loadAnime()
    }

    override fun onPause() {
        super.onPause()
        if (textToSpeech?.isSpeaking == true) textToSpeech = null
    }

    private fun getIntentData() {
        idOfAnime = arguments?.getString(IntentKey.ID_OF_ANIME, "") ?: ""
        println("Anime Id received: $idOfAnime")
    }

    private fun setUpDefaults() {
        setViewsBasedOnDeviceDimensions()
        initTextToSpeech()
        binding.btnPlayEpisodes.disable()
        binding.tvDesc.maxLines = 4
        binding.layoutTrailer.apply {
            tvVideoTitle.gone()
            ivContentHolder.gone()
            ivPlay.visible()
            tvTrailer.visible()
            viewBlackFade.visible()
            ivThumbnail.layoutParams.height = deviceWidth() / 2
            ivThumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        binding.apply {
            ivShare.layoutParams.width = deviceWidth() / 5
            ivGenerateBarcode.layoutParams.width = deviceWidth() / 5
            llLike.layoutParams.width = deviceWidth() / 5
        }
        binding.tvMoreLikeThis.text = "More like this"
        binding.tvEpisodes.text = "Episodes"
        binding.tvTrailerTitle.text = "Trailer"
        binding.tvDescTitle.text = "Description"
    }

    private fun setUpRecyclerView() {
        binding.rvRecommendations.apply {
            layoutManager = LinearLayoutManager(nnContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendationsAdapter
        }

        binding.rvEpisodes.apply {
            layoutManager = LinearLayoutManager(nnContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = episodesAdapter
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
                favoriteAnime = Favorite(
                    checkThisOut = checkThisOutList[secureRandom.nextInt(8)],
                    title = data?.data?.titles?.en?.trimJunk() ?: data?.data?.titles?.rj?.trimJunk(),
                    desc = data?.data?.descriptions?.en?.trimJunk() ?: data?.data?.descriptions?.jp?.trimJunk(),
                    score = data?.data?.score ?: 0,
                    coverImage = data?.data?.coverImage,
                    bannerImage = data?.data?.bannerImage,
                    date = timeNow,
                    id = data?.data?.id ?: -1
                )
                animeFromApi = data
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

        animeViewModel.getEpisodesList().observe(viewLifecycleOwner) { it: ApiState<EpisodeList?>? ->
            it ?: return@observe

            it onSuccess { data: EpisodeList?, message: String ->
                utils.asyncLog(message = "Episode List chan: %s", data)
                episodesAdapter.episodesList = data?.data?.documents ?: emptyList()
                episodesAdapter.notifyDataSetChanged()
                binding.btnPlayEpisodes.enable()
            }

            it onFailure { data: EpisodeList?, message: String ->
                utils.showToast(message = message, context = nnContext)
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
        nnActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isEnabled) return
                isEnabled = false
                nnActivity.supportFragmentManager.popBackStackImmediate()
            }
        })

        // https://github.com/zxing/zxing
        // https://github.com/JiashuWu/Android-Barcode
        // https://github.com/journeyapps/zxing-android-embedded
        binding.ivGenerateBarcode.onSafeClick {
            if (idOfAnime.isNullOrBlankOrNaOrNullString()) return@onSafeClick
            CoroutineScope(Default).launch {
                val barcodeBitmap = try {
                    val bitMatrix = MultiFormatWriter().encode(idOfAnime, BarcodeFormat.QR_CODE, 660, 660)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val pixels = IntArray(width * height)
                    for (i in 0 until height) {
                        for (j in 0 until width) {
                            if (bitMatrix[j, i]) {
                                pixels[i * width + j] = -0x1000000
                            } else {
                                pixels[i * width + j] = -0x1
                            }
                        }
                    }
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                    bitmap
                } catch (e: WriterException) {
                    Timber.e(e)
                    null
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }

                withContext(Main) {
                    val dialogBinding = DialogGeneratedBarcodeBinding.inflate(LayoutInflater.from(context), binding.root, false).apply {
                        ivGeneratedBarcode.setImageBitmap(barcodeBitmap ?: return@withContext)
                    }
                    AlertDialog.Builder(nnContext).apply {
                        setTitle("Scan")
                        setCancelable(false)
                        setView(dialogBinding.root)
                        setPositiveButton("Done") { dialog, which -> }
                        create()
                        show()
                    }
                }
            }
        }

        binding.ivShare.onSafeClick {
            nnActivity.shareViaApps(
                imageDrawableOrUrl = nnContext.drawable(R.drawable.saitama),
                imageView = binding.ivCoverImage,
                title = "Fav Anime App",
                subtitle = "Fav Anime App"
            )
        }

        binding.llLike.onSafeClick {
            binding.btnLike.performClick() // So when u click tvLikeState it inturn clicks btnLike which performs its action
        }

        // https://github.com/jd-alexander/LikeButton
        // https://www.studytonight.com/post/implement-twitter-heart-button-like-animation-in-android-app
        binding.btnLike.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton) {
                activity.vibrate(positiveAction = true)
                val favoriteSoundsList = listOf(R.raw.favorite, R.raw.favorite2, R.raw.favorite3)
                context.playSound(favoriteSoundsList.shuffled().first())
                utils.showSnackBar(view = binding.root, message = getString(R.string.added_to_favorites))
                favoritesViewModel.addToFavorites(favoriteAnime ?: return)
            }

            override fun unLiked(likeButton: LikeButton) {
                activity.vibrate(positiveAction = false)
                val unFavoriteSoundsList = listOf(R.raw.unfavorite, R.raw.unfavorite2, R.raw.unfavorite3)
                context.playSound(unFavoriteSoundsList.shuffled().first())
                utils.showSnackBar(view = binding.root, message = getString(R.string.removed_from_favorites))
                favoritesViewModel.removeFromFavorites(favoriteAnime ?: return)
            }
        })

        binding.tvDesc.onSafeClick { it: Pair<View?, Boolean> ->
            if (it.second) binding.tvDesc.maxLines = 50
            else binding.tvDesc.maxLines = 4
        }

        binding.tvReadDesc.onSafeClick {
            startTextToSpeech()
        }

        // https://stackoverflow.com/questions/10713312/can-i-have-onscrolllistener-for-a-scrollview
        binding.scrollViewAnimeDetail.setOnScrollChangeListener { view: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            println("Scrolled to $scrollX, $scrollY from $oldScrollX, $oldScrollY")
        }

        binding.layoutTrailer.root.onSafeClick {
            if (animeFromApi?.data?.trailerUrl?.contains("www.youtube.com") == true) {
                val videoId = animeFromApi?.data?.trailerUrl?.substringAfterLast("/")
                val intent = Intent(nnActivity, YoutubeVideoActivity::class.java).apply { putExtra(KEY_YOUTUBE_VIDEO_ID, videoId) }
                startActivity(intent)
            }
        }

        recommendationsAdapter.setRecommendationsItemClickListener { animeId: String ->
            nnActivity.showAnimeDetailsOfThis(animeId)
        }

        binding.btnPlayEpisodes.onSafeClick {
            val intent = Intent(nnActivity, ExoPlayerActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKey.EPISODE_LIST, ArrayList<Episode?>(episodesAdapter.episodesList))
            }
            startActivity(intent)
        }

        episodesAdapter.setEpisodeItemClickListener { episode: Episode ->
            val intent = Intent(nnActivity, ExoPlayerActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKey.EPISODE_LIST, ArrayList<Episode?>().apply { add(episode) })
            }
            startActivity(intent)
        }
    }

    private fun loadAnime() {
        if (null != animeViewModel.getAnime().value) return
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                animeViewModel.loadAnime(idOfAnime)
            },
            offlineWork = {
                animeViewModel.loadAnime(idOfAnime)
            }
        )
    }

    private fun updateUI(anime: Anime?) {
        favoritesViewModel.getFavoritesLiveList().observe(viewLifecycleOwner) { favoritesList: List<Favorite>? ->
            favoritesList ?: return@observe
            binding.btnLike.isLiked = favoritesList.any { it.id == favoriteAnime?.id } == true
        }
        favoritesViewModel.loadFavoriteAnimeListFromDb()
        binding.apply {
            episodesAdapter.bannerImage = anime?.data?.bannerImage ?: anime?.data?.coverImage ?: ""
            glide.load(anime?.data?.coverImage).into(binding.ivCoverImage)
            doAfter(1.seconds()) {
                Blurry.with(nnContext)
                    .radius(25)
                    .sampling(4)
                    .animate(500)
                    .capture(binding.ivCoverImage)
                    .getAsync {
                        binding.ivBlurBackground.setImageDrawable(BitmapDrawable(resources, it))
                    }
            }
        }
        binding.apply {
            tvTitle.text = anime?.data?.titles?.en?.trimJunk() ?: anime?.data?.titles?.rj?.trimJunk() ?: getString(R.string.na)
            tvDesc.text = anime?.data?.descriptions?.en?.trimJunk() ?: anime?.data?.descriptions?.jp?.trimJunk()
            if (tvDesc.text.isBlank()) cardDesc.gone()
            val rating = (anime?.data?.score?.div(10F))?.div(2F) ?: 0F
            println("Converted Rating: $rating vs Actual Rating: ${anime?.data?.score}")
            ratingAnimeDetail.rating = rating
        }
        if (anime?.data?.trailerUrl?.contains("www.youtube.com") == true) {
            val youtubeVideoId = anime.data.trailerUrl?.substringAfterLast("/")
            glide.load(youtubeVideoId?.toYoutubeThumbnailUrl()).into(binding.layoutTrailer.ivThumbnail)
        } else {
            binding.layoutTrailer.root.gone()
            binding.tvTrailerTitle.gone()
        }
        binding.apply {
            tvStatus.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_status),
                value = AnimeStatus.getText(anime?.data?.status?.toByte()) ?: getString(R.string.na)
            )
            tvFormat.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_format_s),
                value = AnimeFormats.getText(anime?.data?.format?.toByte()) ?: getString(R.string.na)
            )
            tvStartDate.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_start_date_s),
                value = anime?.data?.startDate?.utcTimeTo(DateType.dd_MMM_yyyy) ?: getString(R.string.na)
            )
            tvEndDate.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_end_date_s),
                value = anime?.data?.endDate?.utcTimeTo(DateType.dd_MMM_yyyy) ?: getString(R.string.na)
            )
            tvSeasonPeriod.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_season_period_s),
                value = AnimeSeasonPeriod.getText(anime?.data?.seasonPeriod?.toByte()) ?: getString(R.string.na)
            )
            tvSeasonYear.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_season_year_s),
                value = (anime?.data?.seasonYear ?: getString(R.string.na)).toString()
            )
            tvEpisodeCount.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_episode_count_s),
                value = (anime?.data?.episodesCount ?: getString(R.string.na)).toString()
            )
            tvEpisodeDuration.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_episode_duration_s),
                value = anime?.data?.episodeDuration?.toString().plus(" minutes")
            )
            tvWeeklyAiringDate.text = nnContext.getCustomText(
                key = getString(R.string.anime_detail_weekly_airing_date_s),
                value = AnimeWeeklyAiringDay.getText(anime?.data?.weeklyAiringDay?.toByte()) ?: getString(R.string.na)
            )
        }
        anime?.data?.genres?.forEach { it: String ->
            val chip = Chip(nnContext).apply {
                text = it
                isCheckable = false
                isClickable = false
//                chipBackgroundColor = ColorStateList.valueOf(nnContext.color(R.color.white))
//                setTextColor(nnContext.color(R.color.purple_500))
                elevation = 4f
                onSafeClick {
                    // TODO call genres api with grid view 3 columns
                }
            }
            binding.chipGroupGenre.addView(chip)
        }
        if (anime?.data?.genres.isNullOrEmpty()) binding.scrollViewGenres.gone()
        loadEpisodes()
        loadRecommendations(anime)
        nnActivity.findViewById<CardView>(R.id.card_bottom_nav).gone()
    }

    private fun loadEpisodes() {
        if (null != animeViewModel.getEpisodesList().value) return
        val animeId = idOfAnime.toIntOrNull() ?: 0
        fun getEpisodeListFromApi() = animeViewModel.getEpisodesList(animeId = animeId, number = null, isDub = null, locale = null)
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = { getEpisodeListFromApi() },
            offlineWork = { getEpisodeListFromApi() }
        )
    }

    private fun loadRecommendations(anime: Anime?) {
        // TODO call api for every aniem id
        CoroutineScope(IO).launch {
            val recommendationsList = anime?.data?.recommendations?.mapNotNull { animeId: Int ->
                animeDao.getAnimeById(animeId.toString())
            } ?: emptyList()

            withContext(Main) {
                recommendationsAdapter.recommendationsList = recommendationsList
                if (recommendationsAdapter.recommendationsList.isEmpty()) {
                    binding.rvRecommendations.gone()
                    binding.tvMoreLikeThis.gone()
                }
            }
        }
    }

    // https://stackoverflow.com/questions/5042197/android-set-height-and-width-of-custom-view-programmatically
    private fun setViewsBasedOnDeviceDimensions() {
        binding.ivCoverImage.layoutParams.apply {
            width = deviceWidth() / 3
            height = deviceHeight() / 4
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val result: Int? = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Timber.w("Language not supported for Text-to-Speech!")
                }
            }
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                CoroutineScope(Main).launch { utils.showToast("Started reading $utteranceId", nnContext) }
            }

            override fun onDone(utteranceId: String) {
                CoroutineScope(Main).launch { utils.showToast("Finished reading $utteranceId", nnContext) }
            }

            override fun onError(utteranceId: String) {
                CoroutineScope(Main).launch { utils.showToast("Error reading $utteranceId", nnContext) }
            }
        })
    }

    private fun startTextToSpeech() {
        val utteranceId = getString(R.string.description)
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId) }
        val textToSpeak = binding.tvDesc.text
        textToSpeech?.apply {
            speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            playSilentUtterance(1000, TextToSpeech.QUEUE_ADD, utteranceId) // Stay silent for 1000 ms
        }
    }
}
