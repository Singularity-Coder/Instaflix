package com.singularitycoder.viewmodelstuff2.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.model.ErrorResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class Utils {

    @Inject lateinit var retrofit: Retrofit

    fun getErrorMessage(context: Context, error: Any?): String {
        val somethingIsWrong = context.getString(R.string.something_is_wrong)
        try {
            val parentJsonObj = (error as? JSONObject) ?: return somethingIsWrong
            val childJsonObj = parentJsonObj.getJSONObject("error")
            val errorCode = childJsonObj.getString("code")
            val message = childJsonObj.getString("message") ?: ""
            return if (message.isNullOrBlankOrNaOrNullString()) somethingIsWrong else message
        } catch (e: JsonParseException) {
            Timber.e("getErrorMessage: ${e.localizedMessage}")
            return somethingIsWrong
        }
    }

    fun getErrorMessageWithGson(context: Context, error: Any?, gson: Gson): String {
        val somethingIsWrong = context.getString(R.string.something_is_wrong)
        try {
            val parentJsonObj = (error as? JSONObject) ?: return somethingIsWrong
            val errorResponse = gson.fromJson(parentJsonObj.toString(), ErrorResponse::class.java)
            val errorCode = errorResponse.error.errorCode
            val message = errorResponse.error.message
            return if (message.isNullOrBlankOrNaOrNullString()) somethingIsWrong else message
        } catch (e: Exception) {
            Timber.e("getErrorMessageWithGson: ${e.localizedMessage}")
            return somethingIsWrong
        }
    }

    // To generate error, change the version of the API to a random number
    fun getErrorMessageWithRetrofit(context: Context, errorResponseBody: ResponseBody?): String {
        val somethingIsWrong = context.getString(R.string.something_is_wrong)
        try {
            errorResponseBody ?: return somethingIsWrong
            val errorResponse = retrofit.responseBodyConverter<ErrorResponse>(ErrorResponse::class.java, arrayOf()).convert(errorResponseBody) ?: return somethingIsWrong
            val errorCode = errorResponse.error.errorCode
            val message = errorResponse.error.message
            return if (message.isNullOrBlankOrNaOrNullString()) somethingIsWrong else message
        } catch (e: Exception) {
            Timber.e("getErrorMessageWithRetrofit: ${e.localizedMessage}")
            return somethingIsWrong
        }
    }
}

fun String?.isNullOrBlankOrNaOrNullString(): Boolean = this.isNullOrBlank() || "null" == this.toLowCase().trim() || "na" == this.toLowCase().trim()

fun String.toLowCase(): String = this.lowercase(Locale.getDefault())

fun String.toUpCase(): String = this.uppercase(Locale.getDefault())

fun String.capFirstChar(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

