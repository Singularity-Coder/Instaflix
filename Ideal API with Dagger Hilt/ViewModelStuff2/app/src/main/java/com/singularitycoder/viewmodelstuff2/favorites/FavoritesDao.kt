package com.singularitycoder.viewmodelstuff2.favorites

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table

@Dao
interface FavoritesDao {

    // Single Item CRUD ops ------------------------------------------------------------------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Transaction
    @Query("SELECT * FROM ${Table.FAVORITES} WHERE id LIKE :id LIMIT 1")
    suspend fun getFavoriteById(id: String): Favorite?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(favorite: Favorite)

    @Delete
    suspend fun delete(favorite: Favorite)
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @Query("SELECT * FROM ${Table.FAVORITES}")
    fun getAll(): LiveData<List<Favorite>>

    @Query("DELETE FROM ${Table.FAVORITES}")
    suspend fun deleteAll()
}
