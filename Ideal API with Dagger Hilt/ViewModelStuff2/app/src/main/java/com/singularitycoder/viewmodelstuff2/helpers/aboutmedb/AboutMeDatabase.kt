package com.singularitycoder.viewmodelstuff2.helpers.aboutmedb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.viewmodelstuff2.more.dao.AboutMeDao
import com.singularitycoder.viewmodelstuff2.more.model.GitHubProfileQueryModel

@Database(
    entities = [GitHubProfileQueryModel::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DataConverter::class,
    RepositoryOwnerConverter::class,
    FollowersConverter::class,
    FollowingConverter::class,
    PinnedItemsConverter::class,
    StarredRepositoriesConverter::class,
    TopRepositoriesConverter::class,
    EdgeConverter::class,
    NodeConverter::class,
    OwnerConverter::class,
    PrimaryLanguageConverter::class,
    NodeXConverter::class,
    OwnerXConverter::class,
    PrimaryLanguageXConverter::class
)
abstract class AboutMeDatabase : RoomDatabase() {
    abstract fun aboutMeDao(): AboutMeDao
}

