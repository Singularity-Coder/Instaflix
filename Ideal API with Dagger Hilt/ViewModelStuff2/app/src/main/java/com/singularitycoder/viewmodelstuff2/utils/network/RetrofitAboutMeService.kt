package com.singularitycoder.viewmodelstuff2.utils.network

import com.singularitycoder.viewmodelstuff2.aboutme.model.GitHubProfileQueryModel
import retrofit2.http.Body
import retrofit2.http.POST

// https://stackoverflow.com/questions/58429501/unable-to-invoke-no-args-constructor-for-retrofit2-call
// When we use suspend, we should have the return type of the model data rather than Call<Type>. Else we get this weird error - Unable to invoke no-args constructor for retrofit2.Call. Registering an InstanceCreator with Gson for this type may fix this problem.
// So either remove suspend to use Call<Type> or remove Call wrapping to handle with the type directly. Error handling will be done with exceptions
// You can also use Response class with suspend funcs

interface RetrofitAboutMeService {

    @POST("graphql")
    suspend fun getGithubProfileData(@Body body: Map<String, String>): GitHubProfileQueryModel
}
