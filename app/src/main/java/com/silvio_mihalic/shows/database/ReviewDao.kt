package com.silvio_mihalic.shows.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.silvio_mihalic.shows.model.database.ReviewEntity
import io.reactivex.Single

@Dao
interface ReviewDao {

    @Query("SELECT * FROM review WHERE show_id IS :showId")
    fun getAllReviewsLiveData(showId: String): LiveData<List<ReviewEntity>>

    @Query("SELECT * FROM review WHERE show_id IS :showId")
    fun getAllReviewsList(showId: String): Single<List<ReviewEntity>>

    @Query("SELECT * FROM review WHERE id IS :reviewId")
    fun getReview(reviewId: String): LiveData<ReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewReview(review: ReviewEntity)
}
