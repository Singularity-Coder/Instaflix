package com.singularitycoder.viewmodelstuff2.notifications.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.singularitycoder.viewmodelstuff2.databinding.LayoutNotificationAnimeItemBinding
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.helpers.extensions.dpToPx
import com.singularitycoder.viewmodelstuff2.helpers.extensions.setMargins
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toIntuitiveDateTime
import javax.inject.Inject

class NotificationsAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
    private val notificationsListDiffer = AsyncListDiffer(this, diffUtil)
    var notificationsList: List<Notification>
        get() = notificationsListDiffer.currentList
        set(value) = notificationsListDiffer.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = LayoutNotificationAnimeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(itemBinding = itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NotificationViewHolder).setData(notificationsList[position])
    }

    override fun getItemCount(): Int = notificationsList.size

    // Position gets messed up without itemViewType
    // https://stackoverflow.com/questions/44932450/wrong-order-of-restored-items-in-recyclerview
    override fun getItemViewType(position: Int): Int = position

    inner class NotificationViewHolder(val itemBinding: LayoutNotificationAnimeItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(notification: Notification) {
            itemBinding.apply {
                tvCheckThisOut.text = notification.checkThisOut
                tvTitle.text = notification.title ?: "Title Not Available"
                viewCustomRating.rating = notification.score
                glide.load(notification.coverImage).into(ivCoverImage)
                tvDateTime.text = notification.date.toIntuitiveDateTime()
//                ivCoverImage.setOnClickListener {
//                    ivCoverImage.apply {
//                        layoutParams.height -= 16
//                        layoutParams.width -= 16
//                    }
//                    cardCoverImage.apply {
//                        layoutParams.height -= 16
//                        layoutParams.width -= 16
//                        elevation = 2F
//                    }
//                }
                if (bindingAdapterPosition == 0) this.root.setMargins(start = 0, top = 0, end = 0, bottom = 82.dpToPx()) // Since RecyclerView is reversed
                if (bindingAdapterPosition == notificationsList.lastIndex) this.root.setMargins(start = 0, top = 8.dpToPx(), end = 0, bottom = 0)
            }
        }
    }
}
