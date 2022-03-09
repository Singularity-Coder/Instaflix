package com.singularitycoder.viewmodelstuff2.notifications.repository

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseRepository
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeErrorResponse
import com.singularitycoder.viewmodelstuff2.anime.model.RandomAnimeListData
import com.singularitycoder.viewmodelstuff2.notifications.dao.NotificationsDao
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.helpers.NotificationUtils
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.checkThisOutList
import com.singularitycoder.viewmodelstuff2.helpers.network.*
import com.singularitycoder.viewmodelstuff2.helpers.service.AnimeForegroundService
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.helpers.utils.timeNow
import com.singularitycoder.viewmodelstuff2.helpers.utils.wait
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.net.HttpURLConnection
import java.security.SecureRandom
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val dao: NotificationsDao,
    private val retrofit: RetrofitAnimeService,
    private val context: Context,
    private val utils: GeneralUtils,
    private val gson: Gson,
    private val networkState: NetworkState,
    private val secureRandom: SecureRandom,
    private val notificationUtils: NotificationUtils
): BaseRepository() {
    val randomAnimeListFromApi = MutableLiveData<NetRes<RandomAnimeListData?>>()
    val randomAnimeListFromDb = MutableLiveData<NetRes<List<Notification>>>()

    suspend fun getRandomAnimeListFromDb() {
        // get list in reverse chron order
        randomAnimeListFromDb.postValue(NetRes(status = Status.SUCCESS, data = dao.getAll()))
    }

    @ExperimentalCoroutinesApi
    suspend fun getRandomAnimeListFromApi(shouldStartForegroundService: Boolean = false): LiveData<NetRes<RandomAnimeListData?>> = suspendCancellableCoroutine<LiveData<NetRes<RandomAnimeListData?>>> { continuation ->
        if (networkState.isOffline()) {
            randomAnimeListFromApi.postValue(NetRes(status = Status.SUCCESS, data = null, message = context.getString(R.string.offline)))
            if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
            return@suspendCancellableCoroutine
        }

        randomAnimeListFromApi.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.SHOW))
        if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)

        retrofit.getRandomAnimeList(count = 1, nsfw = true).enqueue(object : Callback<RandomAnimeListData> {
            override fun onResponse(call: Call<RandomAnimeListData>, response: Response<RandomAnimeListData>) {
                Timber.i("Current Thread Name: ${Thread.currentThread().name}")
                CoroutineScope(IO).launch {

                    suspend fun loadNotificationDataIntoDb() {
                        val singleAnime = response.body()?.data?.firstOrNull()
                        val notification = Notification(
                            aniListId = singleAnime?.aniListId ?: -1,
                            checkThisOut = checkThisOutList[secureRandom.nextInt(8)],
                            title = singleAnime?.titles?.en,
                            score = singleAnime?.score ?: 0,
                            coverImage = singleAnime?.coverImage,
                            date = timeNow
                        )
                        dao.insert(notification)
                        getRandomAnimeListFromDb()
                    }

                    suspend fun showNotification() = withContext(Main) {
                        notificationUtils.showRandomAnimeNotification(
                            animeData = response.body()?.data?.firstOrNull(),
                            mainActivity = MainActivity::class.java
                        )
                    }

                    fun sendNotificationBadgeBroadcast() {
                        val intent = Intent(IntentKey.ACTION_NOTIFICATION_BADGE).putExtra(IntentKey.DATA_SHOW_NOTIFICATION_BADGE, true)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    }

                    fun startAndUpdateAnimeForegroundService() {
                        val intent = Intent(context, AnimeForegroundService::class.java).apply {
                            val singleAnime = response.body()?.data?.first()
                            putParcelableArrayListExtra(IntentKey.NOTIF_FOREGROUND_SERVICE_RANDOM_ANIME, ArrayList<Parcelable?>().apply { add(singleAnime) })
                        }
                        ContextCompat.startForegroundService(context, intent) // This handles starting service according to API level. Cmd + B to see impl
                    }

                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        if (null == response.body() || response.body()?.data.isNullOrEmpty()) {
                            randomAnimeListFromApi.postValue(NetRes(status = Status.SUCCESS, message = context.getString(R.string.nothing_to_show)))
                            if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
                            wait()
                            randomAnimeListFromApi.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
                            if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
                            return@launch
                        }

                        if (!shouldStartForegroundService) {
                            loadNotificationDataIntoDb()
                            showNotification()
                            sendNotificationBadgeBroadcast()
                        }
                        if (shouldStartForegroundService) startAndUpdateAnimeForegroundService()

                        randomAnimeListFromApi.postValue(NetRes(status = Status.SUCCESS, data = response.body()))
                        if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
                    } else {
                        val errorMessage = utils.getErrorMessageWithRetrofit<AnimeErrorResponse>(context = context, errorResponseBody = response.errorBody())
                        randomAnimeListFromApi.postValue(NetRes(status = Status.ERROR, message = errorMessage))
                        if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
                    }

                    wait()
                    randomAnimeListFromApi.postValue(NetRes(status = Status.LOADING, loadingState = LoadingState.HIDE))
                    if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
                }
            }

            override fun onFailure(call: Call<RandomAnimeListData>, t: Throwable) {
                Timber.i("Current Thread Name: ${Thread.currentThread().name}")
                Timber.e("Something went wrong while fetching anime list: ${t.message}")
                randomAnimeListFromApi.value = NetRes(status = Status.ERROR, message = t.message)
                if (continuation.isActive) continuation.resume(value = randomAnimeListFromApi, onCancellation = null)
            }
        })
    }
}
