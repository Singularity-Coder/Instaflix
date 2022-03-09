package com.singularitycoder.viewmodelstuff2.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.Notif
import com.singularitycoder.viewmodelstuff2.helpers.extensions.drawable
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toDrawable
import com.singularitycoder.viewmodelstuff2.helpers.extensions.trimJunk
import timber.log.Timber
import java.io.IOException

// https://www.youtube.com/watch?v=Ge4_4ZnAAX8
// >= API 26 which is Oreo, u need channels
// To have context inside this class, extend it from ContextWrapper
// Use NotificationManagerCompat is available in support lib. Used for posting notifications to support older versions of android
class NotificationUtils(context: Context?) : ContextWrapper(context) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val animeWorkerChannel = createChannel(
                channelId = Notif.ANIME_RECOMMENDATION_WORKER.channelId,
                channelName = Notif.ANIME_RECOMMENDATION_WORKER.channelName,
                channelDescription = "This notification is part of the 15 minute random anime alerts!"
            )

            val animeForegroundServiceChannel = createChannel(
                channelId = Notif.ANIME_FOREGROUND_SERVICE.channelId,
                channelName = Notif.ANIME_FOREGROUND_SERVICE.channelName,
                channelDescription = "This notification is part of the Foreground Service random anime alerts!"
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(animeWorkerChannel)
                createNotificationChannel(animeForegroundServiceChannel)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ): NotificationChannel = NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        enableLights(true) // Shows notification indicator on device
        enableVibration(true) // Device vibrates
        description = channelDescription // Shows up in device notification settings
        lightColor = Color.RED // Notification indicator color on device
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Shows notification content even when the device is locked
    }

    // Title, text, smallIcon are mandatory
    fun showRandomAnimeNotification(
        animeData: AnimeData?,
        mainActivity: Class<out Any>
    ) {
        val title = animeData?.titles?.en?.trimJunk() ?: "Not Available"
        val desc = animeData?.descriptions?.en?.trimJunk() ?: "Not Available"
        val coverImage = try {
            animeData?.coverImage?.toDrawable()?.toBitmap()
        } catch (e: IOException) {
            Timber.i(e.message)
            null
        }
        val intent = Intent(this, mainActivity).apply {
            putParcelableArrayListExtra(IntentKey.NOTIF_WORKER_RANDOM_ANIME, ArrayList<Parcelable?>().apply { add(animeData) })
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            Notif.ANIME_RECOMMENDATION_WORKER.ordinal,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val largeNotifStyle = NotificationCompat.BigPictureStyle()
            .setBigContentTitle(title)
            .setSummaryText(desc)
            .bigLargeIcon(drawable(R.drawable.ic_launcher)?.toBitmap())

        if (null != coverImage) largeNotifStyle.bigPicture(coverImage)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            largeNotifStyle.showBigPictureWhenCollapsed(true)
        }

        val smallNotifStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .setSummaryText(desc)
            .bigText(desc)

        val notifStyles = listOf(largeNotifStyle, smallNotifStyle)

        val notification = NotificationCompat.Builder(this, Notif.ANIME_RECOMMENDATION_WORKER.channelId)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(R.drawable.ic_baseline_favorite_border_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For < API 26 this is a must
            .setStyle(notifStyles.shuffled().first())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // When u click on notif it removes itself from the notif drawer
            .build()

        NotificationManagerCompat.from(this).notify(Notif.ANIME_RECOMMENDATION_WORKER.ordinal, notification)
    }

    fun createForegroundServiceNotification(animeData: AnimeData?): Notification {
        val title = animeData?.titles?.en?.trimJunk() ?: "Not Available"
        val desc = animeData?.descriptions?.en?.trimJunk() ?: "Not Available"
        val coverImage = try {
            animeData?.coverImage?.toDrawable()?.toBitmap()
        } catch (e: IOException) {
            Timber.i(e.message)
            null
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putParcelableArrayListExtra(IntentKey.NOTIF_FOREGROUND_SERVICE_RANDOM_ANIME, java.util.ArrayList<Parcelable?>().apply { add(animeData) })
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            Notif.ANIME_FOREGROUND_SERVICE.ordinal,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val largeNotifStyle = NotificationCompat.BigPictureStyle()
            .setBigContentTitle(title)
            .setSummaryText(desc)
            .bigLargeIcon(drawable(R.drawable.ic_launcher)?.toBitmap())
        if (null != coverImage) largeNotifStyle.bigPicture(coverImage)

        val notification = NotificationCompat.Builder(this, Notif.ANIME_FOREGROUND_SERVICE.channelId)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(R.drawable.ic_baseline_favorite_border_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For < API 26 this is a must
            .setStyle(largeNotifStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // When u click on notif it removes itself from the notif drawer
            .build()

        return notification
    }
}
