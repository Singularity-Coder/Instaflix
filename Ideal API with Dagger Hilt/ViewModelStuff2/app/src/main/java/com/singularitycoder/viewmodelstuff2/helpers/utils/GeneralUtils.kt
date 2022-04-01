package com.singularitycoder.viewmodelstuff2.helpers.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.TimingLogger
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.singularitycoder.viewmodelstuff2.BuildConfig
import com.singularitycoder.viewmodelstuff2.MainActivity
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeErrorResponse
import com.singularitycoder.viewmodelstuff2.databinding.LayoutCustomToastBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.dpToPx
import com.singularitycoder.viewmodelstuff2.helpers.extensions.isNullOrBlankOrNaOrNullString
import com.singularitycoder.viewmodelstuff2.more.model.AboutMeErrorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import timber.log.Timber
import java.io.*
import java.lang.reflect.Type
import javax.inject.Inject
import kotlin.reflect.KClass

// Easy Memory Leaks - Static context

// This Utils class should not be a singleton as it deals with context stuff. Move to Activity Module
class GeneralUtils @Inject constructor(
    val retrofit: Retrofit,
    val gson: Gson
) : Cloneable {

    @Throws(InstantiationException::class)
    private fun utils() {
        throw InstantiationException("This class is not for instantiation")
    }

    // https://www.geeksforgeeks.org/marker-interface-java/
    // https://stackoverflow.com/questions/49053432/how-to-clone-object-in-kotlin
    override fun clone(): Any = super.clone()

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
            val errorResponse = gson.fromJson(parentJsonObj.toString(), AnimeErrorResponse::class.java) ?: return somethingIsWrong
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
    fun <T> getErrorMessageWithRetrofit(context: Context, errorResponseBody: ResponseBody?): String {
        fun getDefaultMessage(): String = context.getString(R.string.something_is_wrong)
        try {
            errorResponseBody ?: return getDefaultMessage()
            fun getErrorResponse(): T? = retrofit.responseBodyConverter<T>(KClass::class.java, arrayOf()).convert(errorResponseBody)
            fun getMessage(): String = when (getErrorResponse()) {
                is AnimeErrorResponse -> (getErrorResponse() as? AnimeErrorResponse)?.error?.message ?: getDefaultMessage()
                is AboutMeErrorResponse -> (getErrorResponse() as? AboutMeErrorResponse)?.message ?: getDefaultMessage()
                else -> getDefaultMessage()
            }
            return if (getMessage().isNullOrBlankOrNaOrNullString()) getDefaultMessage() else getMessage()
        } catch (e: Exception) {
            Timber.e("getErrorMessageWithRetrofit: ${e.localizedMessage}")
            return getDefaultMessage()
        }
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

    inline fun <reified T : Any> listToString(list: ArrayList<T>?): String? {
        if (null == list) return null
        val type: Type = object : TypeToken<ArrayList<T>?>() {}.type
        return gson.toJson(list, type)
    }

    inline fun <reified T : Any> stringToList(string: String?): ArrayList<T>? {
        if (null == string) return null
        val type: Type = object : TypeToken<ArrayList<T>?>() {}.type
        return gson.fromJson<ArrayList<T>?>(string, type)
    }

    inline fun <reified T : Any> objectToString(obj: T?): String? {
        obj ?: return null
        return gson.toJson(obj)
    }

    inline fun <reified T : Any> stringToObject(string: String?): T? {
        string ?: return null
        return gson.fromJson(string, T::class.java)
    }

    private var toast: Toast? = null
    // https://stackoverflow.com/questions/32815407/is-it-normal-to-show-a-single-toast-multiple-times-on-android
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

// https://stackoverflow.com/questions/51299641/difference-of-setvalue-postvalue-in-mutablelivedata
// https://kinnrot.github.io/live-data-pitfall-you-should-be-aware-of/
// setValue(): Sets the value. If there are active observers, the value will be dispatched to them. This method must be called from the main thread.
// postValue(): Posts a task to a main thread to set the given value. If you called this method multiple times before a main thread executed a posted task, only the last value would be dispatched.
// Also calling get value too quickly just after postValue might get you old data
suspend fun wait(duration: Long = 500L) = delay(timeMillis = duration)

fun waitFor(duration: Long) = SystemClock.sleep(duration)

// Get Epoch Time
val timeNow: Long
    get() = System.currentTimeMillis()

// https://github.com/mslalith/focus_launcher
fun doAfter(duration: Long, task: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(task, duration)
}

fun showSettingsDialog(context: Context) {
    AlertDialog.Builder(context).apply {
        setTitle("Give Permissions!")
        setMessage("We need you to grant the permissions for the feature to work!")
        setPositiveButton("OK") { dialog, which ->
            dialog.cancel()
            openSettings(context)
        }
        setNegativeButton("CANCEL") { dialog, which -> dialog.cancel() }
        show()
    }
}

// Open device app settings to allow user to enable permissions
fun openSettings(context: Context) {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

fun getDeviceSize(): Point = try {
    Point(deviceWidth(), deviceHeight())
} catch (e: Exception) {
    e.printStackTrace()
    Point(0, 0)
}

fun deviceWidth() = Resources.getSystem().displayMetrics.widthPixels

fun deviceHeight() = Resources.getSystem().displayMetrics.heightPixels

fun checkFunctionExecutionTimings() {
    TimingLogger("TAG", "hasValidInput").apply {
        addSplit("")
        dumpToLog()
    }
}

fun Activity.getLocalBitmapUri(imageView: ImageView): Uri? {
    if (imageView.drawable !is BitmapDrawable) return null
    // Store image to default external storage directory
    return try {
        // Use methods on Context to access package-specific directories on external storage. This way, you don't need to request external read/write permission.
        val file = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png")
        val out = FileOutputStream(file)
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap // Extract Bitmap from ImageView drawable
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        out.close()
        // Warning: This will fail for API >= 24, use a FileProvider as shown below instead.
        Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap? {
    val output = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)
    val roundPx = pixels.toFloat()
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}

@Throws(IOException::class)
private fun getBytes(inputStream: InputStream): ByteArray? {
    val byteBuffer = ByteArrayOutputStream()
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)
    var len = 0
    while (inputStream.read(buffer).also { len = it } != -1) {
        byteBuffer.write(buffer, 0, len)
    }
    return byteBuffer.toByteArray()
}

fun getBitmapImageUri(context: Context, bitmap: Bitmap): Uri? {
    // Stores saved image
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "CompressedImage", null)
    return Uri.parse(path)
}

fun getTempBitmapImageUri(bitmap: Bitmap): Uri? {
    // Stores image temporarily
    var tempDir: File = Environment.getExternalStorageDirectory()
    tempDir = File(tempDir.absolutePath.toString() + "/.temp/")
    tempDir.mkdir()
    var tempFile: File? = null
    try {
        tempFile = File.createTempFile("CompressedImage", ".jpg", tempDir)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes)
    val bitmapData: ByteArray = bytes.toByteArray()

    // Write bytes in file
    try {
        val fos = FileOutputStream(tempFile).apply {
            write(bitmapData)
            flush()
            close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return Uri.fromFile(tempFile)
}

fun glideLargeImage(context: Context?, imgUrl: String?, imageView: ImageView, empty1: String?) {
    val requestOptions: RequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.color.purple_100)
        .error(android.R.color.holo_red_dark)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    Glide.with(context!!).load(imgUrl)
        .transition(withCrossFade(300)) // giving issues
        .apply(requestOptions)
        .into(imageView)
}

// Glide Big with error handling
fun glideImageWithErrHandle(context: Context?, imgUrl: String?, imageView: ImageView, empty1: String?) {
    Glide.with(context!!)
        .load(imgUrl)
        .apply(
            RequestOptions()
                .placeholder(R.color.purple_100)
                .error(R.color.purple_100)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
        )
        .listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                Toast.makeText(context, "Bad Image! Loading default!", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean = false

        })
        .transition(withCrossFade())
        .into(imageView)
}
