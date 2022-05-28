package com.singularitycoder.viewmodelstuff2.anime.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.databinding.ListItemRecommendationBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import javax.inject.Inject

class RecommendationsAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<AnimeData>() {
        override fun areItemsTheSame(oldItem: AnimeData, newItem: AnimeData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AnimeData, newItem: AnimeData): Boolean {
            return oldItem == newItem
        }
    }

    private val recommendationsListDiffer = AsyncListDiffer(this, diffUtil)
    var recommendationsList: List<AnimeData?>
        get() = recommendationsListDiffer.currentList
        set(value) = recommendationsListDiffer.submitList(value)

    private var itemClickListener: (animeId: String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendationsViewHolder(itemBinding = itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecommendationsViewHolder) holder.setData(recommendationsList?.get(position))
    }

    override fun getItemCount(): Int = recommendationsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setRecommendationsItemClickListener(listener: (animeId: String) -> Unit) {
        itemClickListener = listener
    }

    inner class RecommendationsViewHolder(val itemBinding: ListItemRecommendationBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(anime: AnimeData?) {
            itemBinding.apply {
                val imageUrl = if (anime?.coverImage.isNullOrBlankOrNaOrNullString()) {
                    anime?.bannerImage
                } else {
                    anime?.coverImage
                }
                glide.load(imageUrl).into(ivRecommendation)
                root.onSafeClick { itemClickListener.invoke(anime?.id.toString()) }
            }
        }
    }
}
