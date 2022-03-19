package com.singularitycoder.viewmodelstuff2.anime.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table

@Dao
interface AnimeDao {

    // Single Item CRUD ops ------------------------------------------------------------------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeData)

    @Transaction
    @Query("SELECT * FROM ${Table.ANIME_DATA} WHERE aniListId LIKE :aniListId LIMIT 1")
    suspend fun getAnimeByAniListId(aniListId: String): AnimeData?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(anime: AnimeData)

    @Delete
    suspend fun delete(anime: AnimeData)

    @Transaction
    @Query("SELECT * FROM ${Table.ANIME_DATA}  WHERE seasonYear LIKE :year LIMIT 1")
    suspend fun getAnimeByYear(year: String): AnimeData
    // ---------------------------------------------------------------------------------------------------------------------------------------------

    // All of the parameters of the Insert method must either be classes annotated with Entity or collections/array of it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animeList: List<AnimeData>)

    @Query("SELECT * FROM ${Table.ANIME_DATA}")
    fun getAllAnimeListLiveData(): LiveData<List<AnimeData>>

    @Query("SELECT * FROM ${Table.ANIME_DATA}")
    fun getAll(): List<AnimeData>

    @Query("DELETE FROM ${Table.ANIME_DATA}")
    suspend fun deleteAll()
}
