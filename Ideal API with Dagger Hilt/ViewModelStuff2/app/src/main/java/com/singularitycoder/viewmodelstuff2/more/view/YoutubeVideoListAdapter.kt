package com.singularitycoder.viewmodelstuff2.more.view

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.singularitycoder.viewmodelstuff2.databinding.ListItemVideoBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toYoutubeThumbnailUrl
import com.singularitycoder.viewmodelstuff2.helpers.utils.deviceWidth
import com.singularitycoder.viewmodelstuff2.more.model.YoutubeVideo
import javax.inject.Inject

class YoutubeVideoListAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<YoutubeVideo>() {
        override fun areItemsTheSame(oldItem: YoutubeVideo, newItem: YoutubeVideo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: YoutubeVideo, newItem: YoutubeVideo): Boolean {
            return oldItem == newItem
        }
    }

    private val youtubeVideoDiffer = AsyncListDiffer(this, diffUtil)
    var youtubeVideoList: List<YoutubeVideo>
        get() = youtubeVideoDiffer.currentList
        set(value) = youtubeVideoDiffer.submitList(value)

    private var youtubeVideoClickListener: (videoId: String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YoutubeVideoViewHolder(itemBinding = itemBinding)
    }

    override fun getItemCount(): Int = youtubeVideoList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is YoutubeVideoViewHolder) holder.setData(youtubeVideoList[position])
    }

    override fun getItemViewType(position: Int): Int = position

    fun setVideoItemClickListener(listener: (videoId: String) -> Unit) {
        youtubeVideoClickListener = listener
    }

    inner class YoutubeVideoViewHolder(val itemBinding: ListItemVideoBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(youtube: YoutubeVideo?) {
            itemBinding.apply {
                root.tag = bindingAdapterPosition
                tvVideoTitle.text = youtube?.title
                ivThumbnail.apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams.height = deviceWidth() / 2
                }
                root.setOnClickListener {
                    youtubeVideoClickListener.invoke(youtube?.videoId ?: return@setOnClickListener)
                }
            }
            glide.load(youtube?.videoId?.toYoutubeThumbnailUrl()).into(itemBinding.ivThumbnail)
            glide.asBitmap().load(youtube?.videoId?.toYoutubeThumbnailUrl()).into(object : CustomTarget<Bitmap>(2, 2) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    itemBinding.ivContentHolder.setImageDrawable(BitmapDrawable(itemBinding.root.context.resources, resource))
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    itemBinding.ivContentHolder.setImageDrawable(null)
                }
            })
        }
    }
}
