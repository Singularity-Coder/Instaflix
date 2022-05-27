package com.singularitycoder.viewmodelstuff2.favorites

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table

@Entity(tableName = Table.FAVORITES)
data class Favorite(
    @PrimaryKey var id: Int = 0,
    var checkThisOut: String,
    var title: String? = "",
    var score: Int,
    var coverImage: String? = "",
    var bannerImage: String? = "",
    var date: Long = 0L,
    var desc: String? = "",
    var coverImageBase64: String = "",
    var bannerImageBase64: String = ""
) {
    // This is for Room - Entities and POJOs must have a usable public constructor. You can have an empty constructor or a constructor whose parameters match the fields (by name and type).
    constructor() : this(
        id = 0,
        checkThisOut = "",
        title = "",
        score = 0,
        coverImage = "",
        bannerImage = "",
        date = 0L,
        desc = "",
        coverImageBase64 = "",
        bannerImageBase64 = ""
    )
}
