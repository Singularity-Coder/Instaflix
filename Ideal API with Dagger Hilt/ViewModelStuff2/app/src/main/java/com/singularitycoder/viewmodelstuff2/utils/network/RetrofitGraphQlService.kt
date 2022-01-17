package com.singularitycoder.viewmodelstuff2.utils.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitGraphQlService {

    @Headers("Content-Type: application/json")
    @POST("graphql")
    suspend fun getGithubProfileData(
        @Body body: String,
        @Header("Authorization") authorization: String
    ): Response<String>
}
