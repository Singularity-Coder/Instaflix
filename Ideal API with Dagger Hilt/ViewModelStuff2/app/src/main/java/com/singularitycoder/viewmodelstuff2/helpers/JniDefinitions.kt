package com.singularitycoder.viewmodelstuff2.helpers

// https://developer.android.com/training/articles/perf-jni
// https://medium.com/@shalommathews05/android-modularization-make-a-reusable-c-module-intermediate-45c921f2608e
// https://www.algolia.com/blog/engineering/android-ndk-how-to-reduce-libs-size/
// https://www.journaldev.com/28972/android-jni-application-ndk
// https://blog.mindorks.com/getting-started-with-android-ndk-android-tutorial

// LLDB: It is used by Android Studio to debug the native code present in your project.
// NDK: Native Development Kit(NDK) is used to code in C and C++ i.e. native languages for Android.
// CMake: It is an open-source system that manages the build process in an operating system and a compiler-independent manner.

// Use APK Split or APK Bundle to reduce APK size
object JniDefinitions {

    // Used to load the 'native-lib.cpp' library on file startup.
    init {
        System.loadLibrary("native-lib")
    }

    // A native method that is implemented by the 'native-lib' native library, packaged with this app.
    // Get a string from the native code in our Kotlin code using JNI.
    external fun getApIAuthToken(apiType: Int): String
    external fun getAniApiBaseUrl(): String
    external fun getGithubApiBaseUrl(): String
}

enum class ApIAuthToken(val value: Int) {
    ANI_API(value = 0),
    GITHUB(value = 1)
}
