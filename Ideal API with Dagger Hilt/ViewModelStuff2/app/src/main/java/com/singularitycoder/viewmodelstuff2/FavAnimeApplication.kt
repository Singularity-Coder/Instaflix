package com.singularitycoder.viewmodelstuff2

import android.app.Application
import android.os.StrictMode
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import android.os.StrictMode.VmPolicy
import java.lang.RuntimeException

// Initialise objects here if u want those instances to be available always. Mark their contexts with weak refernce if u wnat then Garbage collected
// Application class is invoked before any app components start

@HiltAndroidApp
class FavAnimeApplication : Application() {

    init {
        // https://stackoverflow.com/questions/56911580/w-system-a-resource-failed-to-call-release
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)

        // https://stackoverflow.com/questions/56911580/w-system-a-resource-failed-to-call-release
        if (BuildConfig.DEBUG) {
            try {
                Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
                    .invoke(null, true)
            } catch (e: ReflectiveOperationException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.i("Phone ran out of memory")
    }
}
