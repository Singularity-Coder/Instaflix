package com.singularitycoder.viewmodelstuff2.about.repository

import android.content.Context
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.about.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.anime.dao.FavAnimeDao
import com.singularitycoder.viewmodelstuff2.utils.Utils
import com.singularitycoder.viewmodelstuff2.utils.network.ApiState
import com.singularitycoder.viewmodelstuff2.utils.network.NetworkState
import com.singularitycoder.viewmodelstuff2.utils.network.RetrofitService
import javax.inject.Inject

class AboutRepository @Inject constructor(
    val dao: FavAnimeDao,
    val retrofit: RetrofitService,
    val context: Context,
    val utils: Utils,
    val gson: Gson,
    val networkState: NetworkState
) {

//    suspend fun getAboutData(): ApiState<GitHubProfileQueryModel?> {
//        return ApiState.Success(null)
//    }
}
