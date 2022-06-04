package com.singularitycoder.viewmodelstuff2.more.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.FragmentMoreBinding
import com.singularitycoder.viewmodelstuff2.helpers.BarcodeScanActivity
import com.singularitycoder.viewmodelstuff2.helpers.constants.FragmentsTags
import com.singularitycoder.viewmodelstuff2.helpers.constants.Gender
import com.singularitycoder.viewmodelstuff2.helpers.constants.animeQuoteList
import com.singularitycoder.viewmodelstuff2.helpers.decode
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MoreFragment : BaseFragment() {

    companion object {
        fun newInstance() = MoreFragment()
    }

    val viewModel: AnimeViewModel by viewModels()
    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity
    private lateinit var binding: FragmentMoreBinding

    private val barcodeScanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents.isNullOrBlankOrNaOrNullString()) {
            binding.root.showSnackBar(message = getString(R.string.something_is_wrong), anchorView = activity?.findViewById(R.id.bottom_nav))
            return@registerForActivityResult
        }

        CoroutineScope(Default).launch {
            val encryptedIdOfAnime = result.contents
            val idOfAnime = decode(encryptedIdOfAnime)
            Timber.i(
                """
                Scanned result a.k.a Encrypted Id of anime: ${result.contents}
                Decrypted Id of anime: $idOfAnime
            """.trimIndent()
            )

            withContext(Main) {
                nnActivity.showAnimeDetailsOfThis(idOfAnime)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDefaults()
        setUpAnimeQuotes()
        setUpUserActionListeners()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // This is still useful in some cases over onResume.
        // Basically called when this fragment is visible to user or user can see this only when this fragment is in background and comes to foreground.
        // So this wont get triggered when you launch it. Only when you come back from another fragment.
    }

    private fun setUpDefaults() {
        val gender = Gender.values().toList().shuffled().first()
        binding.tvGreeting.text = if (gender == Gender.MALE) Gender.MALE.value else Gender.FEMALE.value
    }

    private fun setUpAnimeQuotes() {
        val animeQuote = animeQuoteList.shuffled().first()
        binding.tvQuote.text = context?.getHtmlFormattedQuote(quote = animeQuote.quote, author = animeQuote.author)
    }

    private fun setUpUserActionListeners() {
        binding.clAnimeQuotes.setOnClickListener { setUpAnimeQuotes() }

        // https://github.com/journeyapps/zxing-android-embedded
        // https://github.com/journeyapps/zxing-android-embedded/releases
        binding.cardScanBarcode.onSafeClick {
            val scanOptions = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Scan Aniflix Barcode")
                captureActivity = BarcodeScanActivity::class.java
                setTorchEnabled(true)
                setOrientationLocked(true)
                setBeepEnabled(true)
            }
            barcodeScanLauncher.launch(scanOptions)
        }

        binding.apply {
            switchRandomWorker.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) nnContext.startAnimeRecommendationWorker() else nnContext.stopAnimeRecommendationWorker()
            }
            switchRandomForegroundService.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) nnActivity.startAnimeForegroundService() else nnActivity.stopAnimeForegroundService()
            }
        }

        binding.apply {
            tvShareContacts.onSafeClick {
                nnContext.shareSmsToAll()
            }
            tvShareApk.onSafeClick {
                nnActivity.shareApk()
            }
            tvShareApps.onSafeClick {
                nnActivity.shareViaApps(
                    imageDrawableOrUrl = nnContext.drawable(R.drawable.saitama),
                    imageView = binding.ivQuoteBackground,
                    title = "Fav Anime App",
                    subtitle = "Fav Anime App"
                )
            }
            tvShareEmail.onSafeClick {
                nnContext.shareViaEmail(email = "Friend's Email", subject = "Fav Anime App", desc = "Fav Anime App")
            }
            tvShareWhatsapp.onSafeClick {
                nnContext.shareViaWhatsApp(whatsAppPhoneNum = "0000000000")
            }
            tvShareSms.onSafeClick {
                nnContext.shareViaSms(phoneNum = "0000000000")
            }
        }

        binding.cardAbout.onSafeClick {
            nnActivity.showScreen(fragment = AboutMeFragment.newInstance(), tag = FragmentsTags.ABOUT.value, isAdd = true)
        }

        // https://stackoverflow.com/questions/36143802/how-to-detect-the-position-of-the-scroll-nestedscrollview-android-at-the-bottom
        binding.nestedScrollMore.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    Timber.i("Scroll DOWN")
                    nnActivity.findViewById<BottomNavigationView>(R.id.bottom_nav).gone()
                }
                scrollY < oldScrollY -> {
                    Timber.i("Scroll UP")
                    nnActivity.findViewById<BottomNavigationView>(R.id.bottom_nav).visible()
                }
                scrollY == 0 -> Timber.i("TOP SCROLL")
                scrollY == v.measuredHeight - v.getChildAt(0).measuredHeight -> Timber.i("BOTTOM SCROLL")
            }
        })
    }
}
