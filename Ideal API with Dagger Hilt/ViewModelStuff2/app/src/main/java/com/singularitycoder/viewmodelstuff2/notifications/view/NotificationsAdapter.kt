package com.singularitycoder.viewmodelstuff2.notifications.view

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
import com.singularitycoder.viewmodelstuff2.helpers.extensions.shareViaSms
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import javax.inject.Inject

class NotificationsAdapter @Inject constructor(val glide: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var openCardPosition: Int = -1
    private var closedCardPosition: Int = -1
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

    private var notificationClickListener: (animeId: String) -> Unit = {}

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

    fun setNotificationViewClickListener(listener: (animeId: String) -> Unit) {
        notificationClickListener = listener
    }

    inner class NotificationViewHolder(val itemBinding: LayoutNotificationAnimeItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(notification: Notification) {
            itemBinding.apply {
                tvCheckThisOut.text = notification.checkThisOut
                tvTitle.text = notification.title ?: "Title Not Available"
                val rating = (notification.score.div(10F)).div(2F)
                println("Converted Rating: $rating vs Actual Rating: ${notification.score}")
                ratingNotifAnime.rating = rating
                glide.load(notification.coverImage).into(ivCoverImage)
                tvDateTime.text = notification.date.toIntuitiveDateTime()

                if (bindingAdapterPosition == 0) this.root.setMargins(start = 0, top = 0, end = 0, bottom = 82.dpToPx()) // Since RecyclerView is reversed
                if (bindingAdapterPosition == notificationsList.lastIndex) this.root.setMargins(start = 0, top = 8.dpToPx(), end = 0, bottom = 0)

                root.onSafeClick {
                    if (openCardPosition != -1 && openCardPosition != closedCardPosition) {
                        clNotificationAnimeItem.visible()
                        layoutNotificationAnimeItemExpanded.root.gone()
                        notifyItemChanged(openCardPosition)
                        closedCardPosition = openCardPosition
                    }
                    clNotificationAnimeItem.gone()
                    layoutNotificationAnimeItemExpanded.root.visible()
                    openCardPosition = bindingAdapterPosition
                }
            }

            itemBinding.layoutNotificationAnimeItemExpanded.apply {
                tvCheckThisOut.text = notification.checkThisOut
                tvTitle.text = notification.title ?: "Title Not Available"
                val rating = (notification.score.div(10F)).div(2F)
                println("Converted Rating: $rating vs Actual Rating: ${notification.score}")
                ratingNotifAnime.rating = rating
                glide.load(notification.coverImage).into(ivCoverImage)
                tvDesc.text = if (notification.desc.isNullOrBlankOrNaOrNullString()) {
                    root.context.getString(R.string.no_desc_check_later)
                } else notification.desc
                tvDateTime.text = notification.date.toIntuitiveDateTime()
//
                if (bindingAdapterPosition == 0) this.root.setMargins(start = 0, top = 0, end = 0, bottom = 82.dpToPx()) // Since RecyclerView is reversed
                if (bindingAdapterPosition == notificationsList.lastIndex) this.root.setMargins(start = 0, top = 8.dpToPx(), end = 0, bottom = 0)

                clTopSection.onSafeClick {
                    itemBinding.clNotificationAnimeItem.visible()
                    root.gone()
                }
                cardCoverImage.onSafeClick {
                    // show fresco image viewer and allow setting wallpaper and downloading image
                }
                ivWhatsapp.onSafeClick {
                    root.context.shareViaWhatsApp(whatsAppPhoneNum = "0000000000")
                }
                ivEmail.onSafeClick {
                    root.context.shareViaEmail(
                        email = "Friend's Email",
                        subject = notification.title ?: root.context.getString(R.string.na),
                        desc = notification.desc ?: root.context.getString(R.string.na)
                    )
                }
                ivShare.onSafeClick {
                    if (root.context is MainActivity) {
                        (root.context as MainActivity).shareViaApps(
                            imageDrawableOrUrl = notification.coverImage,
                            imageView = itemBinding.ivCoverImage,
                            title = notification.title ?: root.context.getString(R.string.na),
                            subtitle = notification.desc ?: root.context.getString(R.string.na)
                        )
                    }
                }
                ivMessage.onSafeClick {
                    root.context.shareViaSms(phoneNum = "0000000000")
                }
            }
        }
    }
}
