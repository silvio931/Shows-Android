package com.silvio_mihalic.shows.repository

import com.silvio_mihalic.shows.database.ShowDao
import com.silvio_mihalic.shows.model.api.OneShowResponse
import com.silvio_mihalic.shows.model.database.ShowEntity
import com.silvio_mihalic.shows.networking.ShowsApiService
import io.reactivex.Single
import javax.inject.Inject

interface ShowDetailsRepository {
    fun getOneShowFromApi(id: String): Single<OneShowResponse>
    fun getOneShowFromDatabase(id: String): Single<ShowEntity>
}

class ShowDetailsRepositoryImpl @Inject constructor(
    val showDao: ShowDao,
    private val apiService: ShowsApiService
) : ShowDetailsRepository {

    override fun getOneShowFromApi(id: String): Single<OneShowResponse> {
        return apiService.getOneShow(id)
    }

    override fun getOneShowFromDatabase(id: String): Single<ShowEntity> {
        return showDao.getShow(id)
    }
}
