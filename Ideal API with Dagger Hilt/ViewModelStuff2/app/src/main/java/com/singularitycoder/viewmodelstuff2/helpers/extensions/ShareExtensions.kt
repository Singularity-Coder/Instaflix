package com.singularitycoder.viewmodelstuff2.helpers.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.telephony.SmsManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.singularitycoder.viewmodelstuff2.helpers.utils.getLocalBitmapUri
import com.singularitycoder.viewmodelstuff2.helpers.utils.wait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


// FIXME: Other than whatsapp none of them are working properly on Android 12

fun Context.shareViaSms(phoneNum: String) = try {
    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
        type = "vnd.android-dir/mms-sms"
        putExtra("address", phoneNum)
        putExtra("sms_body", "Message Body check")
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    }
    if (smsIntent.resolveActivity(packageManager) != null) {
        startActivity(smsIntent)
    } else null
} catch (e: Exception) {
}

fun Context.shareViaWhatsApp(whatsAppPhoneNum: String) {
    try {
        // checks if such an app exists or not
        packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
        val uri = Uri.parse("smsto:$whatsAppPhoneNum")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply { setPackage("com.whatsapp") }
        startActivity(Intent.createChooser(intent, "Dummy Title"))
    } catch (e: PackageManager.NameNotFoundException) {
        Toast.makeText(this, "WhatsApp not found. Install from PlayStore.", Toast.LENGTH_SHORT).show()
        try {
            val uri = Uri.parse("market://details?id=com.whatsapp")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) }
            startActivity(intent)
        } catch (e: Exception) {
        }
    }
}

fun Context.shareViaEmail(email: String, subject: String, desc: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null)).apply {
        putExtra(Intent.EXTRA_SUBJECT, "Fav Anime App: $subject")
        putExtra(Intent.EXTRA_TEXT, "Check this out... \n$desc")
    }
    startActivity(Intent.createChooser(intent, "Send email..."))
}

fun Activity.shareViaApps(
    imageDrawableOrUrl: Any?,
    imageView: ImageView?,
    title: String,
    subtitle: String
) {
    if (null != imageDrawableOrUrl && null != imageView) {
        // Ask external storage permission
        shareImageAndText(imageDrawableOrUrl, imageView, title, subtitle)
    } else {
        shareOnlyText(title, subtitle)
    }
}

// https://stackoverflow.com/questions/33222918/sharing-bitmap-via-android-intent
fun Activity.shareImageAndTextViaApps(
    uri: Uri,
    title: String,
    subtitle: String,
    intentTitle: String? = null
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, subtitle)
    }
    startActivity(Intent.createChooser(intent, intentTitle ?: "Share to..."))
}

fun Activity.shareImageAndText(
    imageDrawableOrUrl: Any,
    imageView: ImageView,
    title: String,
    subtitle: String
) {
    Glide.with(this)
        .asBitmap()
        .load(imageDrawableOrUrl)
        .into(object : CustomTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                imageView.setImageBitmap(resource)
            }
            override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
    try {
        val bmpUri = getLocalBitmapUri(imageView) ?: Uri.EMPTY
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, bmpUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, subtitle)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }
        startActivity(Intent.createChooser(intent, "Share image using"))
    } catch (e: Exception) {
    }
}

fun Activity.shareOnlyText(title: String, subtitle: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, subtitle)
    //        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }
        startActivity(Intent.createChooser(intent, "Share to"))
    } catch (e: Exception) {
    }
}

@Throws(IOException::class)
fun Activity.shareApk() = try {
    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + appInfo.publicSourceDir))
        putExtra(Intent.EXTRA_TEXT, "Fav Anime")
    }
    startActivity(Intent.createChooser(intent, "Share it using"))
} catch (e1: PackageManager.NameNotFoundException) {
    e1.printStackTrace()
}

// https://stackoverflow.com/questions/20999876/sending-bulk-sms-using-sms-manager-in-android
fun List<String>.sendBulkSms() = CoroutineScope(IO).launch {
    try {
        val smsManager = SmsManager.getDefault()
        this@sendBulkSms.forEach {
            smsManager.sendTextMessage(it, null, "Fav Anime App: All about anime in one place. Get it now!", null, null)
            wait(1.seconds())
        }
    } catch (e: Exception) {
    }
}

fun Context.shareSmsToAll() = CoroutineScope(IO).launch {
    getContacts().mapNotNull { it.mobileNumber?.trim() }.filter { it.length == 10 }.sendBulkSms()
    withContext(Main) {
        toast("Sending SMS to all contacts!")
    }
}
