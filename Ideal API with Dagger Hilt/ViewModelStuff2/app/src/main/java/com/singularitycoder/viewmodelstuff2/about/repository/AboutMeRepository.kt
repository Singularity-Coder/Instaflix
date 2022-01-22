package com.singularitycoder.viewmodelstuff2.about.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.about.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.utils.GRAPH_QL_GITHUB_PROFILE_QUERY
import com.singularitycoder.viewmodelstuff2.utils.Utils
import com.singularitycoder.viewmodelstuff2.utils.network.ApiState
import com.singularitycoder.viewmodelstuff2.utils.network.LoadingState
import com.singularitycoder.viewmodelstuff2.utils.network.NetworkState
import com.singularitycoder.viewmodelstuff2.utils.network.RetrofitGraphQlService
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class AboutMeRepository @Inject constructor(
    private val retrofit: RetrofitGraphQlService,
    private val context: Context,
    private val utils: Utils,
    private val gson: Gson,
    private val networkState: NetworkState
) {

    val aboutMe = MutableLiveData<ApiState<GitHubProfileQueryModel?>>()

    suspend fun getAboutMe() {
        if (networkState.isOffline()) {
            // TODO DB Calls. Maybe use Realm
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

        if (null == response?.data) {
            aboutMe.postValue(ApiState.Success(response, context.getString(R.string.nothing_to_show)))
            utils.delayUntilNextPostValue()
            aboutMe.postValue(ApiState.Loading(loadingState = LoadingState.HIDE))
            return
        }

        // TODO DB Calls

        aboutMe.postValue(ApiState.Success(response))
        utils.delayUntilNextPostValue() // Without this delay btw consecutive postValue events, only the last event will be considered or rather fast enough for the async operation to happen within postValue
        aboutMe.postValue(ApiState.Loading(loadingState = LoadingState.HIDE))
    }
}
