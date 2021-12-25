package com.singularitycoder.viewmodelstuff2

import android.app.Application
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

// Initialise objects here if u want those instances to be available always. Mark their contexts with weak refernce if u wnat then Garbage collected

@HiltAndroidApp
class FavAnimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.i("Phone ran out of memory")
    }
}