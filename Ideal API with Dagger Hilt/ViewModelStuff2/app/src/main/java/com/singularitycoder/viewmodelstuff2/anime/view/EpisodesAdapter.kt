package com.singularitycoder.viewmodelstuff2.anime.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.singularitycoder.viewmodelstuff2.anime.model.Episode
import com.singularitycoder.viewmodelstuff2.databinding.ListItemEpisodeBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.onSafeClick
import com.singularitycoder.viewmodelstuff2.helpers.utils.deviceHeight
import com.singularitycoder.viewmodelstuff2.helpers.utils.deviceWidth
import javax.inject.Inject

class EpisodesAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }

    private var itemClickListener: (episode: Episode) -> Unit = {}
    private val episodesListDiffer = AsyncListDiffer(this, diffUtil)
    var episodesList: List<Episode>
        get() = episodesListDiffer.currentList
        set(value) = episodesListDiffer.submitList(value)
    var bannerImage: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodesViewHolder(itemBinding = itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EpisodesViewHolder) holder.setData(episodesList[position])
    }

    override fun getItemCount(): Int = episodesList.size

    override fun getItemViewType(position: Int): Int = position

    fun setEpisodeItemClickListener(listener: (episode: Episode) -> Unit) {
        itemClickListener = listener
    }

    inner class EpisodesViewHolder(val itemBinding: ListItemEpisodeBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(episode: Episode) {
            itemBinding.apply {
                root.layoutParams.apply {
                    width = deviceWidth() / 3
                    height = deviceHeight() / 10
                }
                ivThumbnail.layoutParams.apply {
                    width = deviceWidth() / 3
                    height = deviceHeight() / 10
                }
                glide.asBitmap().load(bannerImage).into(ivThumbnail)
                tvEpisodeTitle.text = "Episode ${episode.number}"
                root.onSafeClick { itemClickListener.invoke(episode) }
            }
        }
    }
}
