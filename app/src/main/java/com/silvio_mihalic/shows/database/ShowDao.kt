package com.silvio_mihalic.shows.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.silvio_mihalic.shows.model.database.ShowEntity
import io.reactivex.Single

@Dao
interface ShowDao {

    @Query("SELECT * FROM show")
    fun getAllShowsLiveData(): LiveData<List<ShowEntity>>

    @Query("SELECT * FROM show")
    fun getAllShowsList(): Single<List<ShowEntity>>

    @Query("SELECT * FROM show WHERE id IS :showId")
    fun getShow(showId: String): Single<ShowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllShows(shows: List<ShowEntity>)
}
