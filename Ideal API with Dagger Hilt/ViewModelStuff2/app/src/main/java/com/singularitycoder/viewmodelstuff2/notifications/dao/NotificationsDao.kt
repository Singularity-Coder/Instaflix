package com.singularitycoder.viewmodelstuff2.notifications.dao

import androidx.room.*
import com.singularitycoder.viewmodelstuff2.notifications.model.Notification
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table

@Dao
interface NotificationsDao {

    // Single Item CRUD ops ------------------------------------------------------------------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification)

    @Transaction
    @Query("SELECT * FROM ${Table.NOTIFICATIONS} WHERE id LIKE :id LIMIT 1")
    suspend fun getNotificationById(id: String): Notification?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(notification: Notification)

    @Delete
    suspend fun delete(notification: Notification)
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    @Query("SELECT * FROM ${Table.NOTIFICATIONS}")
    suspend fun getAll(): List<Notification>

    @Query("DELETE FROM ${Table.NOTIFICATIONS}")
    suspend fun deleteAll()
}
