package com.singularitycoder.viewmodelstuff2.anime.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.databinding.LayoutHomeAnimeItemSpotlightBinding
import com.singularitycoder.viewmodelstuff2.databinding.LayoutHomeAnimeItemStandardBinding
import com.singularitycoder.viewmodelstuff2.databinding.LayoutHomeAnimeItemTopMarginBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import javax.inject.Inject

class HomeAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<AnimeData>() {
        override fun areItemsTheSame(oldItem: AnimeData, newItem: AnimeData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AnimeData, newItem: AnimeData): Boolean {
            return oldItem == newItem
        }
    }

    private val homeListDiffer = AsyncListDiffer(this, diffUtil)
    var homeList: List<AnimeData>
        get() = homeListDiffer.currentList
        set(value) = homeListDiffer.submitList(value)

    private var spotlightViewClickListener: (animeId: String) -> Unit = {}
    private var standardViewClickListener: (animeId: String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val topMarginItemBinding = LayoutHomeAnimeItemTopMarginBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val spotLightItemBinding = LayoutHomeAnimeItemSpotlightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val standardItemBinding = LayoutHomeAnimeItemStandardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return when (viewType) {
            HomeItemType.TOP_MARGIN.ordinal -> HomeTopMarginViewHolder(itemBinding = topMarginItemBinding)
            HomeItemType.SPOTLIGHT.ordinal -> HomeSpotLightViewHolder(itemBinding = spotLightItemBinding)
            else -> HomeStandardViewHolder(itemBinding = standardItemBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HomeTopMarginViewHolder -> holder
            is HomeSpotLightViewHolder -> holder.setData(homeList[position])
            is HomeStandardViewHolder -> holder.setData(homeList[position])
        }
    }

    override fun getItemCount(): Int = homeList.size

    // (position - 1) in order to get 4 standard items in the first set of the list
    override fun getItemViewType(position: Int): Int = when {
        position == 0 -> HomeItemType.TOP_MARGIN.ordinal
        position == 1 || (position - 1) % 5 == 0 -> HomeItemType.SPOTLIGHT.ordinal
        else -> HomeItemType.STANDARD.ordinal
    }

    fun setSpotlightViewClickListener(listener: (animeId: String) -> Unit) {
        spotlightViewClickListener = listener
    }

    fun setStandardViewClickListener(listener: (animeId: String) -> Unit) {
        standardViewClickListener = listener
    }

    inner class HomeTopMarginViewHolder(val itemBinding: LayoutHomeAnimeItemTopMarginBinding) : RecyclerView.ViewHolder(itemBinding.root)

    inner class HomeSpotLightViewHolder(val itemBinding: LayoutHomeAnimeItemSpotlightBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(anime: AnimeData) {
            itemBinding.apply {
                tvTitle.text = anime.titles.en
                glide.load(anime.bannerImage).into(ivBannerImage)

                if (bindingAdapterPosition == 0) this.root.setMargins(start = 0, top = 8.dpToPx(), end = 0, bottom = 0)
                if (bindingAdapterPosition == homeList.lastIndex) this.root.setMargins(start = 0, top = 0, end = 0, bottom = 82.dpToPx())

                root.onSafeClick { spotlightViewClickListener.invoke(anime.id.toString()) }
            }
        }
    }

    inner class HomeStandardViewHolder(val itemBinding: LayoutHomeAnimeItemStandardBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(anime: AnimeData) {
            val description = if (anime.descriptions.en.isNullOrBlankOrNaOrNullString()) anime.descriptions.it?.trimJunk() else anime.descriptions.en?.trimJunk()
            itemBinding.apply {
                tvTitle.text = if (anime.titles.en.isNullOrBlankOrNaOrNullString()) "No Title Available" else anime.titles.en
                tvDesc.text = if (description.isNullOrBlankOrNaOrNullString()) "No Description Available" else description
                val rating = (anime.score.div(10F)).div(2F)
                println("Converted Rating: $rating vs Actual Rating: ${anime.score}")
                ratingAnime.rating = rating

                // TODO long press to get enlarged imaged
                glide.load(anime.coverImage).into(ivCoverImage)

                if (bindingAdapterPosition == 0) this.root.setMargins(start = 0, top = 8.dpToPx(), end = 0, bottom = 0)
//                if (bindingAdapterPosition == homeList.lastIndex) this.root.setMargins(start = 0, top = 0, end = 0, bottom = 82.dpToPx())

                root.onSafeClick { standardViewClickListener.invoke(anime.id.toString()) }
            }
        }
    }
}

enum class HomeItemType {
    TOP_MARGIN,
    SPOTLIGHT,
    STANDARD
}
