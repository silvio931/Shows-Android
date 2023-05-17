package com.silvio_mihalic.shows.ui.showDetails

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.silvio_mihalic.shows.model.api.OneShowResponse
import com.silvio_mihalic.shows.model.api.ReviewsResponse
import com.silvio_mihalic.shows.model.database.ReviewEntity
import com.silvio_mihalic.shows.model.database.ShowEntity
import com.silvio_mihalic.shows.model.entity.UiReview
import com.silvio_mihalic.shows.model.entity.UiShow
import com.silvio_mihalic.shows.repository.ReviewsRepository
import com.silvio_mihalic.shows.repository.ShowDetailsRepository
import com.silvio_mihalic.shows.utils.NetworkChecker
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
    private val reviewsRepository: ReviewsRepository,
    private val showDetailsRepository: ShowDetailsRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    companion object {
        private const val EMAIL = "email"
        private const val EMAIL_DEF_VALUE = "email"
    }

    private val networkChecker = NetworkChecker()
    val disposable = CompositeDisposable()

    val emailLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val oneShowLiveData: MutableLiveData<UiShow> by lazy {
        MutableLiveData<UiShow>()
    }

    val allReviewsLiveData: MutableLiveData<List<UiReview>> by lazy {
        MutableLiveData<List<UiReview>>()
    }

    init {
        emailLiveData.value = prefs.getString(EMAIL, EMAIL_DEF_VALUE).toString()
    }

    /**
     * Return data about one show
     */
    fun getOneShow(id: String) {
        if (networkChecker.internetIsConnected()) {
            getOneShowFromApi(id)
        } else {
            getOneShowFromDatabase(id)
        }
    }

    private fun getOneShowFromApi(id: String) {
        showDetailsRepository.getOneShowFromApi(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<OneShowResponse> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: OneShowResponse) {
                    oneShowLiveData.postValue(
                        UiShow(
                            t.show.id,
                            t.show.averageRating,
                            t.show.description,
                            t.show.imageUrl,
                            t.show.noOfReviews,
                            t.show.title
                        )
                    )
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                }
            })
    }

    private fun getOneShowFromDatabase(id: String) {
        showDetailsRepository.getOneShowFromDatabase(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<ShowEntity> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: ShowEntity) {
                    oneShowLiveData.postValue(
                        UiShow(
                            t.id,
                            t.averageRating,
                            t.description,
                            t.imageUrl,
                            t.noOfReviews,
                            t.title
                        )
                    )
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                }
            })
    }

    /**
     * Return list of reviews for one show
     */
    fun getReviews(showId: String, email: String) {
        if (networkChecker.internetIsConnected()) {
            getReviewsFromApi(showId, email)
        } else {
            getReviewsFromDatabase(showId)
        }
    }

    private fun getReviewsFromApi(showId: String, email: String) {
        reviewsRepository.getReviewsFromApi(showId, email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<ReviewsResponse> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: ReviewsResponse) {
                    allReviewsLiveData.postValue(
                        t.reviews.map { review ->
                            UiReview(
                                review.id,
                                review.comment,
                                review.rating,
                                review.showId,
                                review.user.email,
                                review.user.imageUrl
                            )
                        }
                    )
                    reviewsRepository.handleResponseGetReviewsApi(t)
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    allReviewsLiveData.postValue(emptyList())
                }
            })
    }

    private fun getReviewsFromDatabase(showId: String) {
        reviewsRepository.getReviewsFromDatabase(showId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<ReviewEntity>> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: List<ReviewEntity>) {
                    allReviewsLiveData.postValue(
                        t.map { review ->
                            UiReview(
                                review.id,
                                review.comment,
                                review.rating,
                                review.showId,
                                review.userEmail,
                                review.userImageUrl
                            )
                        }.sortedByDescending { review -> review.id }
                    )
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    allReviewsLiveData.postValue(emptyList())
                }
            })
    }

    /**
     * Call a repository function to create a review
     */
    fun createReview(rating: Int, comment: String, showId: Int, email: String) {
        if (networkChecker.internetIsConnected()) {
            reviewsRepository.createReviewApi(rating, comment, showId, email)
        } else {
            reviewsRepository.createReviewDatabase(rating, comment, showId, email)
        }
    }

    /**
     * Return review when it is created
     */
    fun getCreateReviewResult(): LiveData<UiReview> {
        return reviewsRepository.createReviewResultLiveData
    }

    /**
     * Return value when offline review is saved in database
     */
    fun createReviewOfflineResult(): LiveData<Boolean> {
        return reviewsRepository.createOfflineReviewResult
    }

    /**
     * Return review when it is uploaded to server after being created in offline mode
     */
    fun getOfflineReviewsInsertResult(): LiveData<UiReview> {
        return reviewsRepository.offlineReviewsSentToApi
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable != null)
            disposable.dispose()
    }
}
