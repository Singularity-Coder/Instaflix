package com.singularitycoder.viewmodelstuff2.helpers.utils

import android.os.Build
import com.singularitycoder.viewmodelstuff2.helpers.extensions.toLowCase
import timber.log.Timber
import java.io.*
import java.lang.reflect.Method

// https://stackoverflow.com/questions/1101380/determine-if-running-on-a-rooted-device?noredirect=1&lq=1
// https://stackoverflow.com/questions/3424195/determining-if-an-android-device-is-rooted-programmatically
// https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents
// https://stackoverflow.com/questions/20932102/execute-shell-command-from-android/26654728#26654728
// https://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in-the-emulator

// Used in Flutter
fun isEmulator(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("sdk_gphone64_arm64")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
}

// Used in React
fun isProbablyRunningOnEmulator(): Boolean {
    // Android SDK emulator
    return (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
            && Build.FINGERPRINT.endsWith(":user/release-keys")
            && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
            && Build.MODEL.startsWith("sdk_gphone_"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true) // bluestacks
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST == "Build2" //MSI App Player
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.PRODUCT == "google_sdk"
            || System.getProperty("ro.kernel.qemu") == "1" // another Android SDK emulator check
}

fun sudo(vararg strings: String) {
    try {
        val su = Runtime.getRuntime().exec("su")
        val outputStream = DataOutputStream(su.outputStream)
        for (s in strings) {
            outputStream.writeBytes("$s\n")
            outputStream.flush()
        }
        outputStream.writeBytes("exit\n")
        outputStream.flush()
        try {
            su.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        outputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun isRooted(isEmulator: Boolean): Boolean {
    val buildTags = Build.TAGS
    return if (!isEmulator && buildTags != null && buildTags.contains("test-keys")) {
        true
    } else {
        var file = File("/system/app/Superuser.apk")
        if (file.exists()) {
            true
        } else {
            file = File("/system/xbin/su")
            !isEmulator && file.exists()
        }
    }
}

val isDeviceRooted: Boolean
    get() = detectRootMethod1() || detectRootMethod2() || detectRootMethod3()

private fun detectRootMethod1(): Boolean {
    val buildTags = Build.TAGS
    return null != buildTags && buildTags.contains("test-keys")
}

private fun detectRootMethod2(): Boolean {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )
    for (path in paths) {
        if (File(path).exists()) return true
    }
    return false
}

private fun detectRootMethod3(): Boolean {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        val `in` = BufferedReader(InputStreamReader(process.inputStream))
        `in`.readLine() != null
    } catch (t: Throwable) {
        false
    } finally {
        process?.destroy()
    }
}

fun isDeviceRooted2(): Boolean {
    val buildTags = Build.TAGS
    if (null != buildTags && buildTags.contains("test-keys")) return true

    // check if /system/app/Superuser.apk is present
    try {
        val file = File("/system/app/Superuser.apk")
        if (file.exists()) return true
    } catch (e: Exception) {
        Timber.e(e)
    }

    // try executing commands
    return (canExecuteCommand("/system/xbin/which su") || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su"))
}

// executes a command on the system
private fun canExecuteCommand(command: String): Boolean {
    val executedSuccessfully: Boolean = try {
        Runtime.getRuntime().exec(command)
        true
    } catch (e: Exception) {
        false
    }
    return executedSuccessfully
}

private fun executeShellCommand(command: String): Boolean {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec(command)
        true
    } catch (e: java.lang.Exception) {
        false
    } finally {
        if (process != null) {
            try {
                process.destroy()
            } catch (e: java.lang.Exception) {
            }
        }
    }
}

fun isRootAvailable(): Boolean {
    for (pathDir in System.getenv("PATH").split(":").toTypedArray()) {
        if (File(pathDir, "su").exists()) {
            return true
        }
    }
    return false
}

fun isRootGiven(): Boolean {
    if (isRootAvailable()) {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val `in` = BufferedReader(InputStreamReader(process.inputStream))
            val output = `in`.readLine()
            if (output != null && output.toLowCase().contains("uid=0")) return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
    }
    return false
}

// findBinary("su")
fun findBinary(binaryName: String): Boolean {
    var found = false
    if (!found) {
        val places = arrayOf(
            "/sbin/",
            "/system/bin/",
            "/system/xbin/",
            "/data/local/xbin/",
            "/data/local/bin/",
            "/system/sd/xbin/",
            "/system/bin/failsafe/",
            "/data/local/"
        )
        for (where in places) {
            if (File(where + binaryName).exists()) {
                found = true
                break
            }
        }
    }
    return found
}

/**
 * In Development - an idea of ours was to check the if selinux is enforcing - this could be disabled for some rooting apps
 * Checking for selinux mode
 *
 * @return true if selinux enabled
 */
fun isSelinuxFlagInEnabled(): Boolean {
    try {
        val c = Class.forName("android.os.SystemProperties")
        val get: Method = c.getMethod("get", String::class.java)
        val selinux = get.invoke(c, "ro.build.selinux") as String
        return "1" == selinux
    } catch (ignored: Exception) {
    }
    return false
}
