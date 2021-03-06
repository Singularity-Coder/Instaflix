package com.singularitycoder.viewmodelstuff2.notifications.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table

@Entity(tableName = Table.NOTIFICATIONS)
data class Notification(
    @PrimaryKey var aniListId: Long,
    var checkThisOut: String,
    var title: String? = "",
    var score: Int,
    var coverImage: String? = "",
    var date: Long = 0L,
    var desc: String? = "",
    var id: Int = 0,
    var coverImageBase64: String = ""
) {

    constructor() : this(
        aniListId = 0L,
        checkThisOut = "",
        title = "",
        score = 0,
        coverImage = "",
        date = 0L,
        desc = "",
        id = 0,
        coverImageBase64 = ""
    )

    override fun equals(other: Any?): Boolean {
        return aniListId == (other as? Notification)?.aniListId
    }

    override fun hashCode(): Int {
        var result = aniListId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + score
        result = 31 * result + coverImage.hashCode()
        return result
    }
}
