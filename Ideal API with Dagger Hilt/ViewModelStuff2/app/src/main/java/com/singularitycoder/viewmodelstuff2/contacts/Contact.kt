package com.singularitycoder.viewmodelstuff2.contacts

import android.graphics.Bitmap
import android.net.Uri

data class Contact(
    var id: String? = null,
    var name: String? = null,
    var mobileNumber: String? = null,
    var photo: Bitmap? = null,
    var photoURI: Uri? = null
)
