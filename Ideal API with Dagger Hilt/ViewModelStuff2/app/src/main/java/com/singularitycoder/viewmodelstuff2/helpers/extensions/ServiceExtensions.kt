package com.singularitycoder.viewmodelstuff2.helpers.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.*
import com.singularitycoder.viewmodelstuff2.helpers.constants.WorkerTag
import com.singularitycoder.viewmodelstuff2.helpers.service.AnimeForegroundService
import com.singularitycoder.viewmodelstuff2.helpers.service.AnimeRecommendationWorker
import java.util.concurrent.TimeUnit

fun Context.startAnimeForegroundService() {
    val intent = Intent(this, AnimeForegroundService::class.java)
    ContextCompat.startForegroundService(this, intent)
}

fun Activity.stopAnimeForegroundService() {
    val intent = Intent(this, AnimeForegroundService::class.java)
    stopService(intent)
}

fun Context.stopAnimeRecommendationWorker() {
    WorkManager.getInstance(this).cancelAllWorkByTag(WorkerTag.RANDOM_ANIME)
}

fun Context.startAnimeRecommendationWorker() {
    // As soon as network is back, work manager listens to network change and does the work automatically
    val constraintBuilder = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // Do work only when it has internet
        .setRequiresBatteryNotLow(false)
        .setRequiresCharging(false)
        .setRequiresStorageNotLow(false).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setRequiresDeviceIdle(false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setTriggerContentMaxDelay(0, TimeUnit.MINUTES)
                setTriggerContentUpdateDelay(0, TimeUnit.MINUTES)
            }
        }
    startWorker(
        workerClass = AnimeRecommendationWorker::class.java,
        constraints = constraintBuilder.build(),
        isPeriodic = true,
        workTag = WorkerTag.RANDOM_ANIME
    )
}

fun Context.startWorker(
    workerClass: Class<out ListenableWorker?>,
    constraints: Constraints,
    isPeriodic: Boolean = false,
    repeatInterval: Long = 15,
    timeUnit: TimeUnit = TimeUnit.MINUTES,
    workTag: String = "DefaultWorker",
    inputData: Data = Data.Builder().build()
) {
    if (isPeriodic) {
        // Min Repeat interval is 900_000 millis or 15 mins. This does the job first and then does it after every 15 mins
        val periodicWorkRequest = PeriodicWorkRequest.Builder(workerClass, repeatInterval, timeUnit)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workTag)
            .build()

        // https://stackoverflow.com/questions/51612274/check-if-workmanager-is-scheduled-already
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(workTag, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)
    } else {
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(workerClass)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workTag)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(workTag, ExistingWorkPolicy.KEEP, oneTimeWorkRequest)
    }
}
