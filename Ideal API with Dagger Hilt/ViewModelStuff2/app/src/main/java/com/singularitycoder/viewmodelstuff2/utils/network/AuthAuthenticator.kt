package com.singularitycoder.viewmodelstuff2.utils.network

import android.content.Context
import com.singularitycoder.viewmodelstuff2.BuildConfig
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

// We use Authenticators to check if auth token got expired (a.k.a 401 error code) and refresh and update the token.

class AuthAuthenticator @Inject constructor(
    val context: Context,
//    val retrofitService: RetrofitService,
//    val gson: Gson,
//    val utils: Utils
) : Authenticator {

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        return try {
            // If last 3 response are busted then quit asking for new token
            if (response == response.priorResponse && response == response.priorResponse?.priorResponse) return null

            // Call Auth Token API
            // Update Token in preferences
            val newToken = BuildConfig.ANI_API_AUTH_TOKEN // The new refreshed token you get from the API
            response.request.newBuilder()
                .addHeader("Authorization", newToken)
                .build()
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }
}
