package com.singularitycoder.viewmodelstuff2.helpers.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.singularitycoder.viewmodelstuff2.helpers.utils.sudo

fun Context.isDeviceEmulator(): Boolean {
    val androidId = Settings.Secure.getString(this.contentResolver, "android_id")
    return "sdk" == Build.PRODUCT || "google_sdk" == Build.PRODUCT || androidId == null
}

fun Activity.uninstall(app: String = "package:com.example.mypackage") {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = Uri.parse("package:$app")
    }
    startActivity(intent)
}

fun Activity.uninstallOnRootedDevice(app: String = "package:com.example.mypackage") {
    val pkg: String = this.packageName
    val shellCmd = """
        rm -r /data/app/$pkg*.apk
        rm -r /data/data/$pkg
        sync
        reboot

        """.trimIndent()
    sudo(shellCmd)
}
