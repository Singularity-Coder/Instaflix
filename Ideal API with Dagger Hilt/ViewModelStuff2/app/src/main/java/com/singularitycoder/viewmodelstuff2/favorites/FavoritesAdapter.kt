package com.singularitycoder.viewmodelstuff2.favorites

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.LayoutFavoriteAnimeItemBinding
import com.singularitycoder.viewmodelstuff2.databinding.LayoutHomeAnimeItemTopMarginBinding
import com.singularitycoder.viewmodelstuff2.databinding.ListItemHeaderBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.onSafeClick
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toIntuitiveDateTime
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import jp.wasabeef.blurry.Blurry
import java.lang.Exception
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
        set(value) = favoritesListDiffer.submitList(value)

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
            itemBinding.apply {

                tvTitle.text = favorite.title ?: "Title Not Available"
                val rating = (favorite.score.div(10F)).div(2F)
                println("Converted Rating: $rating vs Actual Rating: ${favorite.score}")
                tvRating.text = String.format(Locale.US, "%.1f", rating)
                tvDateTime.text = favorite.date.toIntuitiveDateTime()

                glide.load(favorite.coverImage).into(ivBannerImage)

                /**
                * Center crop scale type is allows this view to expand to full dimensions. What an amazing fluke
                 * */
                glide.asBitmap().load(favorite.coverImage).into(object : CustomTarget<Bitmap>(2, 2) { // LOL. Tried so hard with Blurry to get blur n it was as simple as reducing bitmap size to get the blur.
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        ivContentHolder.setImageDrawable(BitmapDrawable(itemBinding.root.context.resources, resource))
//                        Palette.Builder(resource).generate { it: Palette? ->
//                            it?.let { palette ->
//                                val dominantColor = palette.getDominantColor(ContextCompat.getColor(itemBinding.root.context, R.color.purple_200))
//                                ivContentHolder.setImageDrawable(dominantColor)
//                                Blurry.with(itemBinding.root.context)
//                                    .radius(25)
//                                    .sampling(8)
//                                    .color(dominantColor)
//                                    .async()
//                                    .capture(ivContentHolder)
//                                    .into(ivContentHolder)
//                            }
//                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                        ivContentHolder.setImageDrawable(null)
                    }
                })

//                val bitmap = Blurry.with(itemBinding.root.context)
//                    .radius(25)
//                    .sampling(8)
////                    .color(dominantColor)
//                    .capture(ivContentHolder).get()
//                ivContentHolder.setImageDrawable(BitmapDrawable(itemBinding.root.context.resources, bitmap))

//                Picasso.get().load(favorite.coverImage)
//                    .into(ivContentHolder, object : Callback {
//                        override fun onSuccess() {
//                        }
//
//                        override fun onError(e: Exception?) = Unit
//                    })

//                Blurry.with(itemBinding.root.context).from(favorite.blurredCoverBitmap).into(ivContentHolder)
//                ivContentHolder.setImageDrawable(BitmapDrawable(itemBinding.root.context.resources, favorite.blurredCoverBitmap))

                root.onSafeClick {
                    favoritesClickListener.invoke(favorite.id.toString())
                }
            }
        }
    }
}

enum class FavoritesItemType {
    HEADER,
    STANDARD
}
