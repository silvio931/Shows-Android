package com.silvio_mihalic.shows.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.silvio_mihalic.shows.model.database.ReviewEntityOffline

@Dao
interface ReviewOfflineDao {

    @Query("SELECT * FROM review_offline WHERE show_id IS :showId AND user_email IS :email")
    fun getAllReviews(showId: String, email: String): List<ReviewEntityOffline>

    @Query("SELECT * FROM review_offline WHERE show_id IS :showId")
    fun getAllReviewsLiveData(showId: String): LiveData<List<ReviewEntityOffline>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewReview(review: ReviewEntityOffline)

    @Query("DELETE FROM review_offline WHERE show_id IS :showId AND user_email IS :email")
    fun deleteReview(showId: Int, email: String)
}
