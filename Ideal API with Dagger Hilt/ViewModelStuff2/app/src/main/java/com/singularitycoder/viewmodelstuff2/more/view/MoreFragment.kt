package com.singularitycoder.viewmodelstuff2.more.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.work.WorkManager
import com.singularitycoder.viewmodelstuff2.BaseFragment
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.anime.viewmodel.AnimeViewModel
import com.singularitycoder.viewmodelstuff2.databinding.FragmentMoreBinding
import com.singularitycoder.viewmodelstuff2.helpers.constants.Gender
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.WorkerTag
import com.singularitycoder.viewmodelstuff2.helpers.constants.animeQuoteList
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.service.AnimeForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

        binding.switchRandomWorker.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) nnContext.startAnimeRecommendationWorker() else nnContext.stopAnimeRecommendationWorker()
        }

        binding.switchRandomForegroundService.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) nnActivity.startAnimeForegroundService() else nnActivity.stopAnimeForegroundService()
        }

        binding.cardAbout.setOnClickListener { about() }
    }

    fun about() = Unit
}
