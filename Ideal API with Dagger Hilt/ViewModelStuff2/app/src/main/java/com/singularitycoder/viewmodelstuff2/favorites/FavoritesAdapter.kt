package com.singularitycoder.viewmodelstuff2.favorites

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.singularitycoder.viewmodelstuff2.databinding.LayoutFavoriteAnimeItemBinding
import com.singularitycoder.viewmodelstuff2.databinding.ListItemHeaderBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.gone
import com.singularitycoder.viewmodelstuff2.helpers.extensions.onSafeClick
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toIntuitiveDateTime
import java.util.*
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
        set(value) {
            val listWithDefaultValue = mutableListOf(Favorite()).also { it: MutableList<Favorite> -> it.addAll(value) }
            favoritesListDiffer.submitList(listWithDefaultValue)
        }

    private var favoritesClickListener: (id: String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = LayoutFavoriteAnimeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val itemBindingHeader = ListItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return when (viewType) {
            FavoritesItemType.HEADER.ordinal -> HeaderViewHolder(itemBindingHeader)
            else -> FavoriteViewHolder(itemBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FavoriteViewHolder -> holder.setData(favoritesList[position])
            else -> holder
        }
    }

    override fun getItemCount(): Int = favoritesList.size

    // Position gets messed up without itemViewType
    // https://stackoverflow.com/questions/44932450/wrong-order-of-restored-items-in-recyclerview
    override fun getItemViewType(position: Int): Int = when {
        position == 0 -> FavoritesItemType.HEADER.ordinal
        else -> FavoritesItemType.STANDARD.ordinal
    }

    fun setFavoriteViewClickListener(listener: (animeId: String) -> Unit) {
        favoritesClickListener = listener
    }

    inner class HeaderViewHolder(itemBinding: ListItemHeaderBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.tvHeader.text = "❤️  Favorites"
        }
    }

    inner class FavoriteViewHolder(val itemBinding: LayoutFavoriteAnimeItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(favorite: Favorite) {
            val bannerImage = if (favorite.bannerImage.isNullOrBlank()) {
                favorite.coverImage
            } else {
                favorite.bannerImage
            }
            val rating = (favorite.score.div(10F)).div(2F)
            if (rating == 0.0F) {
                itemBinding.ivRating.gone()
                itemBinding.tvRating.gone()
            }
            println("Converted Rating: $rating vs Actual Rating: ${favorite.score}")

            itemBinding.tvTitle.text = favorite.title ?: "Title Not Available"
            itemBinding.tvRating.text = String.format(Locale.US, "%.1f", rating) // https://stackoverflow.com/questions/2538787/how-to-print-a-float-with-2-decimal-places-in-java
            itemBinding.tvDateTime.text = favorite.date.toIntuitiveDateTime()
            glide.load(bannerImage).into(itemBinding.ivBannerImage)
            glide.asBitmap().load(bannerImage).into(object : CustomTarget<Bitmap>(2, 2) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    /** Center crop scale type is what allows this view to expand to full dimensions. What an amazing fluke **/
                    itemBinding.ivContentHolder.setImageDrawable(BitmapDrawable(itemBinding.root.context.resources, resource))
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView clear it here as you can no longer have the bitmap.
                    itemBinding.ivContentHolder.setImageDrawable(null)
                }
            })

            itemBinding.root.onSafeClick {
                favoritesClickListener.invoke(favorite.id.toString())
            }
        }
    }
}

enum class FavoritesItemType {
    HEADER,
    STANDARD
}
