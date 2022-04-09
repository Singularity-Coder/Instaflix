package com.singularitycoder.viewmodelstuff2.favorites

import androidx.lifecycle.LiveData
import com.singularitycoder.viewmodelstuff2.BaseRepository
import javax.inject.Inject

class FavoritesRepository @Inject constructor(
    private val dao: FavoritesDao,
): BaseRepository() {

    suspend fun addToFavorites(favorite: Favorite) = dao.insert(favorite)

    suspend fun removeToFavorites(favorite: Favorite) = dao.delete(favorite)

    fun getFavoritesLiveList(): LiveData<List<Favorite>> = dao.getAllLiveList()

    fun getFavoritesList(): List<Favorite> = dao.getAll()
}
