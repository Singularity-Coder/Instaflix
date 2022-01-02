package com.singularitycoder.viewmodelstuff2.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.viewmodelstuff2.model.AnimeData
import com.singularitycoder.viewmodelstuff2.utils.TABLE_ANIME_DATA

@Dao
interface FavAnimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animeList: List<AnimeData>)

    @Query("SELECT * FROM $TABLE_ANIME_DATA")
    fun getAll(): LiveData<List<AnimeData>>

    @Transaction
    @Query("SELECT * FROM $TABLE_ANIME_DATA  WHERE seasonYear LIKE :year LIMIT 1")
    suspend fun getAnimeByYear(year: String): AnimeData

    @Query("DELETE FROM $TABLE_ANIME_DATA")
    suspend fun deleteAll()
}