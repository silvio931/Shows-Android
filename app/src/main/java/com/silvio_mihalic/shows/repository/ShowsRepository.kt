package com.silvio_mihalic.shows.repository

import androidx.lifecycle.MutableLiveData
import com.silvio_mihalic.shows.database.ShowDao
import com.silvio_mihalic.shows.model.api.AllShowsResponse
import com.silvio_mihalic.shows.model.api.TopRatedShowsResponse
import com.silvio_mihalic.shows.model.database.ShowEntity
import com.silvio_mihalic.shows.model.entity.UiShow
import com.silvio_mihalic.shows.networking.ShowsApiService
import io.reactivex.Single
import java.util.concurrent.Executors
import javax.inject.Inject

interface ShowsRepository {
    val allShowsLiveData: MutableLiveData<List<UiShow>>
    fun getAllShowsFromApi(): Single<AllShowsResponse>
    fun getAllShowsFromDatabase(): Single<List<ShowEntity>>
    fun getTopRatedShowsFromApi(): Single<TopRatedShowsResponse>
    fun saveShowsInDatabase(response: AllShowsResponse)
}

class ShowsRepositoryImpl @Inject constructor(
    val showDao: ShowDao,
    private val apiService: ShowsApiService
) : ShowsRepository {

    override val allShowsLiveData: MutableLiveData<List<UiShow>> by lazy {
        MutableLiveData<List<UiShow>>()
    }

    override fun getAllShowsFromApi(): Single<AllShowsResponse> {
        return apiService.getAllShows()
    }

    /**
     * Save list of shows retrieved by api in database
     */
    override fun saveShowsInDatabase(response: AllShowsResponse) {
        val showsListApiShowEntity = response.shows.map { show ->
            ShowEntity(
                show.id,
                show.averageRating,
                show.description,
                show.imageUrl,
                show.noOfReviews,
                show.title
            )
        }

        Executors.newSingleThreadExecutor().execute {
            showDao.insertAllShows(showsListApiShowEntity)
        }
    }

    override fun getAllShowsFromDatabase(): Single<List<ShowEntity>> {
        return showDao.getAllShowsList()
    }

    override fun getTopRatedShowsFromApi(): Single<TopRatedShowsResponse> {
        return apiService.getTopRatedShows()
    }
}
