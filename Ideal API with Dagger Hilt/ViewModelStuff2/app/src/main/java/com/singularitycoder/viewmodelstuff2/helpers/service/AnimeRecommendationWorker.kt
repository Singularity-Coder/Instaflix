package com.singularitycoder.viewmodelstuff2.helpers.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.singularitycoder.viewmodelstuff2.notifications.repository.NotificationsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

// Cant do constructor or field injection here

// Step 1: Create Worker

@ExperimentalCoroutinesApi
class AnimeRecommendationWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    // This is how you get dependencies into an android class that cannot be injected by standard ways
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotifRepoEntryPoint {
        fun notificationsRepo(): NotificationsRepository
    }

    override fun doWork(): Result {
        println("Anime worker started!")
        val appContext = context.applicationContext ?: throw IllegalStateException()
        val notifRepoEntryPoint = EntryPointAccessors.fromApplication(appContext, NotifRepoEntryPoint::class.java)
        val notificationsRepository = notifRepoEntryPoint.notificationsRepo()
        CoroutineScope(IO).launch { notificationsRepository.getRandomAnimeListFromApi() }
        return Result.success()
    }
}
