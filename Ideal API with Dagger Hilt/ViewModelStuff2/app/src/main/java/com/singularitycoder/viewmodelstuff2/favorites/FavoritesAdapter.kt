package com.singularitycoder.viewmodelstuff2.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.LayoutNotificationAnimeItemBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import javax.inject.Inject

class FavoritesAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<Favorite>() {
        override fun areItemsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
            return oldItem == newItem
        }
    }

    private val favoritesListDiffer = AsyncListDiffer(this, diffUtil)
    var favoritesList: List<Favorite>
        get() = favoritesListDiffer.currentList
        set(value) = favoritesListDiffer.submitList(value)

    private var favoritesClickListener: (id: String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = LayoutNotificationAnimeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(itemBinding = itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FavoriteViewHolder).setData(favoritesList[position])
    }

    override fun getItemCount(): Int = favoritesList.size

    // Position gets messed up without itemViewType
    // https://stackoverflow.com/questions/44932450/wrong-order-of-restored-items-in-recyclerview
    override fun getItemViewType(position: Int): Int = position

    fun setFavoriteViewClickListener(listener: (animeId: String) -> Unit) {
        favoritesClickListener = listener
    }

    inner class FavoriteViewHolder(val itemBinding: LayoutNotificationAnimeItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(favorite: Favorite) {
            itemBinding.apply {
                tvCheckThisOut.text = favorite.checkThisOut
                tvTitle.text = favorite.title ?: "Title Not Available"
                val rating = (favorite.score.div(10F)).div(2F)
                println("Converted Rating: $rating vs Actual Rating: ${favorite.score}")
                ratingNotifAnime.rating = rating
                glide.load(favorite.coverImage).into(ivCoverImage)
                tvDateTime.text = favorite.date.toIntuitiveDateTime()

                root.onSafeClick {
                    favoritesClickListener.invoke(favorite.id.toString())
                }
            }
        }
    }
}
