package com.singularitycoder.viewmodelstuff2.utils.network

import android.content.Context
import com.singularitycoder.viewmodelstuff2.BuildConfig
import com.singularitycoder.viewmodelstuff2.utils.BASE_URL
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import timber.log.Timber
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

// If you are using some sort of DI library then sending the headers or params through an interceptor. This way we can reuse this interceptor
// You can add additional headers, params, etc using interceptors for every request
// https://futurestud.io/tutorials/retrofit-getting-started-and-android-client
// https://dev.to/ddinorahtovar/headers-interceptors-and-authenticators-with-retrofit-13ma
class AuthInterceptor @Inject constructor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val timeTakenForApiCall = response.sentRequestAtMillis - response.receivedResponseAtMillis

        Timber.i(
            """
                Is redirect: ${response.isRedirect}
                Is Successful: ${response.isSuccessful}
                Time Taken for API Call: $timeTakenForApiCall ms
                Status Code: ${response.code}
                Method: ${request.method}
                URL: ${request.url}
            """.trimIndent()
        )

        if (null != request.header("Authorization")) return response

        // When your redirect url is messed up send this response instead.
        if (BASE_URL !in request.url.toString()) {
            return Response.Builder()
                .code(HttpURLConnection.HTTP_NOT_FOUND)
                .body(response.body)
                .protocol(Protocol.HTTP_2)
                .message("Wrong request")
                .request(chain.request())
                .build()
        }

        val requestBuilder = request.newBuilder()
            .addHeader("Authorization", BuildConfig.ANI_API_AUTH_TOKEN)
            .addHeader("charset", "UTF-8")
            .method(request.method, request.body)

        // A way to add query parameters
        val requestBuilder2 = request.url.newBuilder()
            .addQueryParameter("random", UUID.randomUUID().toString())
            .addQueryParameter("locale", Locale.getDefault().language)

        return chain.proceed(requestBuilder.build())
    }
}
