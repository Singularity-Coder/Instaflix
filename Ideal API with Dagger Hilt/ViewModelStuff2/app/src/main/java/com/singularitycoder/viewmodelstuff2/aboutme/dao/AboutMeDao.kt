package com.singularitycoder.viewmodelstuff2.aboutme.dao

import androidx.room.*
import com.singularitycoder.viewmodelstuff2.aboutme.model.GitHubProfileQueryModel
import com.singularitycoder.viewmodelstuff2.utils.TABLE_ABOUT_ME

@Dao
interface AboutMeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gitHubProfileQueryModel: GitHubProfileQueryModel)

    // Room can run multiple queries under the hood. @Transaction ensures this happens atomically - atomicity provides synchronous operability
    // In multi-thread env, we can have 2 threads trying to modify the same resource. To queue them up, we use either synchronized or atomic keywords
    @Transaction
    @Query("SELECT * FROM $TABLE_ABOUT_ME")
    suspend fun getAboutMe(): GitHubProfileQueryModel?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(gitHubProfileQueryModel: GitHubProfileQueryModel)

    @Query("DELETE FROM $TABLE_ABOUT_ME")
    suspend fun deleteAll()
}
