package com.singularitycoder.viewmodelstuff2

import android.app.Application
import androidx.lifecycle.AndroidViewModel

// When we do an @Inject constructor(item1: Type1, item2: Type2) - Hilt will recognise automatically based on the type u provide in the constructor what dependency needs to be passed
// It reads like this - Inject into constructor Type1 and Type2. As simple as that

open class BaseViewModel<T : BaseRepository> (
    application: Application,
    private val repository: T
) : AndroidViewModel(application) {

}
