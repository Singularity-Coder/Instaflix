package com.singularitycoder.viewmodelstuff2.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table

@Entity(tableName = Table.FAVORITES)
data class Favorite(
    @PrimaryKey var id: Int = 0,
    var checkThisOut: String,
    var title: String? = "",
    var score: Int,
    var coverImage: String? = "",
    var date: Long = 0L,
    var desc: String? = "",
    var coverImageBase64: String = ""
)
