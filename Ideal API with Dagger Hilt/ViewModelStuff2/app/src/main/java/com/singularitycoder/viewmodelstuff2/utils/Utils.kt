package com.singularitycoder.viewmodelstuff2.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.singularitycoder.viewmodelstuff2.anime.view.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.LayoutCustomToastBinding
import com.singularitycoder.viewmodelstuff2.anime.model.ErrorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject

// Easy Memory Leaks
// Static context

class Utils @Inject constructor(
    val retrofit: Retrofit,
    val gson: Gson
) {

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
            val errorResponse = gson.fromJson(parentJsonObj.toString(), ErrorResponse::class.java) ?: return somethingIsWrong
            val errorCode = errorResponse.error.errorCode
            val message = errorResponse.error.message
            return if (message.isNullOrBlankOrNaOrNullString()) somethingIsWrong else message
        } catch (e: Exception) {
            Timber.e("getErrorMessageWithGson: ${e.localizedMessage}")
            return somethingIsWrong
        }
    }

    /**
     * To generate error, change the version of the API to a random number.
     * I am using inner functions here to avoid allocating permanent memory.
     * Theoretically this should improve perf as all ops happen in call stack and memory is released when ops are done.
     * This impacts breakpoint debugging a lot. It gets damn slow
     * TODO Should measure before and after
     * */
    fun getErrorMessageWithRetrofit(context: Context, errorResponseBody: ResponseBody?): String {
        fun getDefaultMessage(): String = context.getString(R.string.something_is_wrong)
        try {
            errorResponseBody ?: return getDefaultMessage()
            fun getErrorResponse(): ErrorResponse? = retrofit.responseBodyConverter<ErrorResponse>(ErrorResponse::class.java, arrayOf()).convert(errorResponseBody)
            fun getErrorCode(): String = getErrorResponse()?.error?.errorCode ?: ""
            fun getMessage(): String = getErrorResponse()?.error?.message ?: getDefaultMessage()
            return if (getMessage().isNullOrBlankOrNaOrNullString()) getDefaultMessage() else getMessage()
        } catch (e: Exception) {
            Timber.e("getErrorMessageWithRetrofit: ${e.localizedMessage}")
            return getDefaultMessage()
        }
    }

    fun getDeviceSize(): Point = try {
        val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
        val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
        Point(deviceWidth, deviceHeight)
    } catch (e: Exception) {
        e.printStackTrace()
        Point(0, 0)
    }

    fun <T> asyncLog(message: String?, vararg objs: T) = CoroutineScope(Default).launch {
        message ?: return@launch
        try {
//            Timber.i(message, objs.map { it: T -> gson.toJson(it) })  // TODO Implement Timber
            println("$message: ${gson.toJson(objs[0])}")
        } catch (e: Exception) {
            Timber.e("Gson parse error: $e")
        }
    }

    fun error(): Nothing = throw IllegalStateException("Shouldn't be here") // Always throw an exception

    fun showSnackBar(
        view: View,
        message: String,
        anchorView: View? = null,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionBtnText: String = "NA",
        action: () -> Unit = {}
    ) {
        Snackbar.make(view, message, duration).apply {
            this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
            if (null != anchorView) this.anchorView = anchorView
            if ("NA" != actionBtnText) setAction(actionBtnText) { action.invoke() }
            this.show()
        }
    }

    inline fun <reified T> listToString(list: ArrayList<T>?): String? {
        if (null == list) return null
        val type: Type = object : TypeToken<ArrayList<T>?>() {}.type
        return gson.toJson(list, type)
    }

    inline fun <reified T> stringToList(string: String?): ArrayList<T>? {
        if (null == string) return null
        val type: Type = object : TypeToken<ArrayList<T>?>() {}.type
        return gson.fromJson<ArrayList<T>?>(string, type)
    }

    inline fun <reified T> objectToString(obj: T?): String? {
        obj ?: return null
        return gson.toJson(obj)
    }

    inline fun <reified T> stringToObject(string: String?): T? {
        string ?: return null
        return gson.fromJson(string, T::class.java)
    }

    private var toast: Toast? = null
    @ExperimentalCoroutinesApi
    fun showToast(
        message: String,
        context: Context,
        duration: Int = Toast.LENGTH_LONG,
        @DrawableRes textImage: Int = R.drawable.ic_android_black_24dp,
    ) {
        try {
            if ((context as? MainActivity)?.isFinishing == true) return
            if (message.isBlank()) return
            if (null != toast) toast?.cancel()

            val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(context)).apply {
                tvCustomToastText.apply {
                    text = message
                    setCompoundDrawablesWithIntrinsicBounds(textImage, 0, 0, 0)
                    compoundDrawablePadding = 12
                }
            }

            toast = Toast(context).apply {
                view = binding.root
                this.duration = duration
                setGravity(Gravity.BOTTOM, 0, 16.dpToPx())
            }
            toast?.show()
        } catch (e: Exception) {
            Timber.e("Something is wrong with the toast: $e")
        }
    }
}

fun String?.isNullOrBlankOrNaOrNullString(): Boolean = this.isNullOrBlank() || "null" == this.toLowCase().trim() || "na" == this.toLowCase().trim()

fun String.toLowCase(): String = this.lowercase(Locale.getDefault())

fun String.toUpCase(): String = this.uppercase(Locale.getDefault())

fun String.capFirstChar(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.trimNewLines(): String = this.replace(oldValue = System.getProperty("line.separator") ?: "\n", newValue = " ")

// Works on Windows, Linux and Mac
// https://stackoverflow.com/questions/11048973/replace-new-line-return-with-space-using-regex
// https://javarevisited.blogspot.com/2014/04/how-to-replace-line-breaks-new-lines-windows-mac-linux.html
fun String.trimNewLinesUniversally(): String = this.replace(regex = Regex(pattern = "[\\t\\n\\r]+"), replacement = " ")

fun String.trimIndentsAndNewLines(): String = this.trimIndent().trimNewLinesUniversally()

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun <T> Gson.toJsonObject(obj: T): JSONObject? = try {
    JSONObject().get(this.toJson(obj)) as? JSONObject
} catch (e: JSONException) {
    Timber.e(e)
    null
} catch (e: Exception) {
    Timber.e(e)
    null
}

