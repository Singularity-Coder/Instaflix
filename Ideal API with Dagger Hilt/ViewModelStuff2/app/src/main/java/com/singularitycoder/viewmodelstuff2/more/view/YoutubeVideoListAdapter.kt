package com.singularitycoder.viewmodelstuff2.more.view

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.ListItemVideoBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toYoutubeThumbnailUrl
import com.singularitycoder.viewmodelstuff2.helpers.utils.deviceWidth
import com.singularitycoder.viewmodelstuff2.more.model.YoutubeVideo

// This is getting inconsistent in setting the list. I am facing same issue. https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
// I think its because of Hilt doing injecting adapter in worker threads, for viewpagers it seems to be sending the adapter/list internally a bit late. Just a guess
class YoutubeVideoListAdapter(
    private val youtubeVideoList: List<YoutubeVideo>,
    private val youtubeVideoClickListener: (videoId: String) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YoutubeVideoViewHolder(itemBinding = itemBinding)
    }

    override fun getItemCount(): Int = youtubeVideoList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is YoutubeVideoViewHolder) holder.setData(youtubeVideoList[position])
    }

    override fun getItemViewType(position: Int): Int = position

    inner class YoutubeVideoViewHolder(val itemBinding: ListItemVideoBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(youtube: YoutubeVideo?) {
            val requestOptions = RequestOptions().placeholder(R.color.purple_100).error(android.R.color.holo_red_dark)
            val glide = Glide.with(itemBinding.root.context).setDefaultRequestOptions(requestOptions)
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
