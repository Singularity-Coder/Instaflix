package com.singularitycoder.viewmodelstuff2.more.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.BaseRepository
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.more.dao.AboutMeDao
import com.singularitycoder.viewmodelstuff2.more.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.helpers.constants.GRAPH_QL_GITHUB_PROFILE_QUERY
import com.singularitycoder.viewmodelstuff2.helpers.network.ApiState
import com.singularitycoder.viewmodelstuff2.helpers.network.LoadingState
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.network.RetrofitAboutMeService
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.helpers.utils.wait
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class MoreRepository @Inject constructor(
    private val dao: AboutMeDao,
    private val retrofit: RetrofitAboutMeService,
    private val context: Context,
    private val utils: GeneralUtils,
    private val gson: Gson,
    private val networkState: NetworkState
): BaseRepository() {

    val aboutMe = MutableLiveData<ApiState<GitHubProfileQueryModel?>>()

    suspend fun getAboutMe() {
        if (networkState.isOffline()) {
            // TODO DB Calls. Maybe use Realm
            aboutMe.postValue(ApiState.Success(dao.getAboutMe(), context.getString(R.string.offline)))
            return
        }

        aboutMe.postValue(ApiState.Loading(loadingState = LoadingState.SHOW))

        val response = try {
            retrofit.getGithubProfileData(body = mapOf("query" to GRAPH_QL_GITHUB_PROFILE_QUERY))
        } catch (e: HttpException) {
            Timber.e("Something went wrong while fetching github data: ${e.message}")
            aboutMe.postValue(ApiState.Error(message = e.message ?: context.getString(R.string.something_is_wrong)))
            null
        } catch (e: Exception) {
            Timber.e("Something went wrong while fetching github data: ${e.message}")
            aboutMe.postValue(ApiState.Error(message = e.message ?: context.getString(R.string.something_is_wrong)))
            null
        }

        if (response?.data == null) {
            aboutMe.postValue(ApiState.Success(response, context.getString(R.string.nothing_to_show)))
            wait()
            aboutMe.postValue(ApiState.Loading(loadingState = LoadingState.HIDE))
            return
        }

        dao.deleteAll()
        dao.insert(response)
        aboutMe.postValue(ApiState.Success(response))
        wait() // Without this delay btw consecutive postValue events, only the last event will be considered or rather fast enough for the async operation to happen within postValue
        aboutMe.postValue(ApiState.Loading(loadingState = LoadingState.HIDE))
    }
}
