package com.silvio_mihalic.shows.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.silvio_mihalic.shows.database.ReviewDao
import com.silvio_mihalic.shows.database.ReviewOfflineDao
import com.silvio_mihalic.shows.model.api.CreateReviewRequest
import com.silvio_mihalic.shows.model.api.CreateReviewResponse
import com.silvio_mihalic.shows.model.api.Review
import com.silvio_mihalic.shows.model.api.ReviewsResponse
import com.silvio_mihalic.shows.model.database.ReviewEntity
import com.silvio_mihalic.shows.model.database.ReviewEntityOffline
import com.silvio_mihalic.shows.model.entity.UiReview
import com.silvio_mihalic.shows.networking.ShowsApiService
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import javax.inject.Inject

interface ReviewsRepository {
    val createReviewResultLiveData: LiveData<UiReview>
    val createOfflineReviewResult: LiveData<Boolean>
    val offlineReviewsSentToApi: LiveData<UiReview>
    fun getReviewsFromApi(showId: String, email: String): Single<ReviewsResponse>
    fun getReviewsFromDatabase(showId: String): Single<List<ReviewEntity>>
    fun handleResponseGetReviewsApi(response: ReviewsResponse)
    fun createReviewApi(rating: Int, comment: String, showId: Int, email: String)
    fun createReviewDatabase(rating: Int, comment: String, showId: Int, email: String)
}

class ReviewsRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao,
    private val reviewOfflineDao: ReviewOfflineDao,
    private val apiService: ShowsApiService
) : ReviewsRepository {

    override fun getReviewsFromApi(showId: String, email: String): Single<ReviewsResponse> {
        addOfflineReviewsToApi(showId, email)
        return apiService.getReviews(showId)
    }

    override fun handleResponseGetReviewsApi(response: ReviewsResponse) {
        val reviewsListEntity = response.reviews.map { review ->
            ReviewEntity(
                review.id,
                review.comment,
                review.rating,
                review.showId,
                review.user.email,
                review.user.imageUrl
            )
        }
        syncDatabaseWithApiGetReviews(reviewsListEntity)
    }

    override fun getReviewsFromDatabase(showId: String): Single<List<ReviewEntity>> {
        return reviewDao.getAllReviewsList(showId)
    }

    /**
     * Add new reviews from api to database
     */
    private fun syncDatabaseWithApiGetReviews(reviewsListApi: List<ReviewEntity>) {
        Executors.newSingleThreadExecutor().execute {
            reviewsListApi.forEach { review ->
                reviewDao.insertNewReview(review)
            }
        }
    }

    /**
     * Return new review when user tries to add a new review
     */
    override val createReviewResultLiveData: MutableLiveData<UiReview> by lazy {
        MutableLiveData<UiReview>()
    }

    /**
     * Call api for creating a new review, return response review if successful, return
     * null if not successful
     */
    override fun createReviewApi(rating: Int, comment: String, showId: Int, email: String) {
        apiService.createReview(CreateReviewRequest(rating, comment, showId))
            .enqueue(object :
                    Callback<CreateReviewResponse> {
                    override fun onResponse(
                        call: Call<CreateReviewResponse>,
                        response: Response<CreateReviewResponse>
                    ) {
                        handleResponseCreateReview(response)
                    }

                    override fun onFailure(call: Call<CreateReviewResponse>, t: Throwable) {
                        createReviewResultLiveData.value = null
                    }
                })
    }

    /**
     * Prepare review data from api if response is successful
     */
    private fun handleResponseCreateReview(response: Response<CreateReviewResponse>) {
        if (response.isSuccessful && response.body()?.review != null) {
            val uiReview = UiReview(
                response.body()?.review!!.id,
                response.body()?.review!!.comment,
                response.body()?.review!!.rating,
                response.body()?.review!!.showId,
                response.body()?.review!!.user.email,
                response.body()?.review!!.user.imageUrl
            )
            createReviewResultLiveData.value = uiReview
            syncDatabaseWithApiCreateReview(response.body()?.review)
        } else {
            createReviewResultLiveData.value = null
        }
    }

    /**
     * Return true when review is added in database in offline mode
     */
    override val createOfflineReviewResult: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    /**
     * Add a review in database in offline mode
     */
    override fun createReviewDatabase(rating: Int, comment: String, showId: Int, email: String) {
        Executors.newSingleThreadExecutor().execute {
            reviewOfflineDao
                .insertNewReview(ReviewEntityOffline(0, comment, rating, showId, email))
        }
        createOfflineReviewResult.value = true
    }

    /**
     * Add new review in database to be synced with api
     */
    private fun syncDatabaseWithApiCreateReview(review: Review?) {
        if (review != null) {
            Executors.newSingleThreadExecutor().execute {
                val reviewEntity =
                    ReviewEntity(
                        review.id,
                        review.comment,
                        review.rating,
                        review.showId,
                        review.user.email,
                        review.user.imageUrl
                    )
                reviewDao.insertNewReview(reviewEntity)
            }
        }
    }

    /**
     * Get all reviews for a specific show created in offline mode and call a function to
     * upload reviews only for logged user to api
     */
    private fun addOfflineReviewsToApi(showId: String, email: String) {
        Executors.newSingleThreadExecutor().execute {
            var listReviews = reviewOfflineDao.getAllReviews(showId, email)
            if (listReviews.isNotEmpty()) {
                insertReviews(listReviews)
            }
        }
    }

    /**
     * Return value when review created in offline mode is uploaded to api
     */
    override val offlineReviewsSentToApi: MutableLiveData<UiReview> by lazy {
        MutableLiveData<UiReview>()
    }

    /**
     * Call an api service to upload every review from a list
     */
    private fun insertReviews(listReviewsEntityOffline: List<ReviewEntityOffline>) {
        listReviewsEntityOffline.forEach { review ->
            apiService.createReview(
                CreateReviewRequest(
                    review.rating,
                    review.comment,
                    review.showId
                )
            )
                .enqueue(object :
                        Callback<CreateReviewResponse> {
                        override fun onResponse(
                            call: Call<CreateReviewResponse>,
                            response: Response<CreateReviewResponse>
                        ) {
                            handleResponseUploadOfflineReview(response)
                        }

                        override fun onFailure(call: Call<CreateReviewResponse>, t: Throwable) {
                            offlineReviewsSentToApi.value = null
                        }
                    })
        }
    }

    /**
     * Read api response, delete reviews for that specific show from database from
     * review_offline table and call a function to sync database with api
     */
    private fun handleResponseUploadOfflineReview(response: Response<CreateReviewResponse>) {
        val review = response.body()?.review
        if (response.isSuccessful && review != null) {
            offlineReviewsSentToApi.value = UiReview(
                review.id,
                review.comment,
                review.rating,
                review.showId,
                review.user.email,
                review.user.imageUrl
            )
            Executors.newSingleThreadExecutor().execute {
                reviewOfflineDao.deleteReview(review.showId, review.user.email)
            }
            syncDatabaseWithApiCreateReview(review)
        } else {
            offlineReviewsSentToApi.value = null
        }
    }
}
