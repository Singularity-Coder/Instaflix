package com.singularitycoder.viewmodelstuff2.helpers.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.helpers.NotificationUtils
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.Notif
import com.singularitycoder.viewmodelstuff2.helpers.extensions.minutes
import com.singularitycoder.viewmodelstuff2.helpers.extensions.doEvery
import com.singularitycoder.viewmodelstuff2.helpers.extensions.seconds
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.notifications.repository.NotificationsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject

// https://www.youtube.com/watch?v=FbpD5RZtbCc
// Find running services: Settings -> System -> Developer Options -> Running Services

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AnimeForegroundService : Service() {

    private lateinit var timer: Timer

    @Inject
    lateinit var notificationRepository: NotificationsRepository

    @Inject
    lateinit var notificationUtils: NotificationUtils

    @Inject
    lateinit var networkState: NetworkState

    // onCreate will be called the first time when we call our service. This gets called only once in the lifecycle of the service. When we create a new new service this gets called again
    override fun onCreate() {
        super.onCreate()
        timer = Timer()
        timer.doEvery(
            duration = 5.minutes(),
            withInitialDelay = 2.seconds(),
        ) {
            notificationRepository.getRandomAnimeListFromApi(shouldStartForegroundService = true)
        }
    }

    // This will be triggered when we start our service, then we can pass an intent with some info. This will be called everytime we start our service. So everytime we pass new anime data this gets triggered
    // This runs on UI thread
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (null != intent) {
            val animeData = intent.getParcelableArrayListExtra<AnimeData>(IntentKey.NOTIF_FOREGROUND_SERVICE_RANDOM_ANIME)?.first()

            if (networkState.isOffline()) return START_NOT_STICKY

            // Without startForeground() android OS will treat this as a normal service and kill it in 1 min
            // Also when you start the service with startForegroundService, within 5 sec if startForeground is not called, system will kill this service
            startForeground(
                Notif.ANIME_FOREGROUND_SERVICE.ordinal,
                notificationUtils.createForegroundServiceNotification(animeData)
            )

            if (animeData?.aniListId == 5E100.toLong()) stopSelf() // This kills this foreground service
        }

        // This defines what happens when the system kills our service
        // START_NOT_STICKY - service is killed and wont start again
        // START_STICKY - system restarts this service as soon as possible but passed intent will be null
        // START_REDELIVER_INTENT - system restarts this service as soon as possible with the last intent
        return START_REDELIVER_INTENT
    }

    // When service is stopped this gets called
    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    // This method is mandatory but we only need it for bound services. Bound services are services where other components can communicate back and forth by binding to it. But this service will be a started service.
    override fun onBind(intent: Intent?): IBinder? = null
}
