package com.silvio_mihalic.shows.ui.shows

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.silvio_mihalic.shows.model.api.AllShowsResponse
import com.silvio_mihalic.shows.model.api.TopRatedShowsResponse
import com.silvio_mihalic.shows.model.api.UploadPictureResponse
import com.silvio_mihalic.shows.model.api.User
import com.silvio_mihalic.shows.model.database.ShowEntity
import com.silvio_mihalic.shows.model.entity.UiShow
import com.silvio_mihalic.shows.repository.ImageUploadRepository
import com.silvio_mihalic.shows.repository.ShowsRepository
import com.silvio_mihalic.shows.utils.NetworkChecker
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class ShowsViewModel @Inject constructor(
    private val showsRepository: ShowsRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    companion object {
        private const val EMAIL = "email"
        private const val URI = "uri"
        private const val URL = "url"
        private const val RECYCLER_VIEW_LAYOUT = "recyclerViewLayout"
        private const val SHOW_TOP_RATED = "showTopRatedShows"
        private const val LAST_CLICKED_SHOW_ID = "lastClickedShowId"
        private const val RECYCLER_SCROLL_DEFAULT_POSITION = -1
        private const val USER_LOGGED_IN = "loggedIn"
    }

    private val networkChecker = NetworkChecker()
    val disposable = CompositeDisposable()

    val allShowsLiveData: MutableLiveData<List<UiShow>> by lazy {
        MutableLiveData<List<UiShow>>()
    }

    val showDetailsStateLiveData: MutableLiveData<ShowDetailsState> by lazy {
        MutableLiveData<ShowDetailsState>()
    }

    val imageUploadResultLiveData: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    init {
        val email = prefs?.getString(EMAIL, "email").toString()
        val avatarUri = Uri.parse(prefs?.getString(URI, null).toString())
        val profileImageUrl = prefs?.getString(URL, null).toString()
        val currentRecyclerViewType = prefs?.getInt(RECYCLER_VIEW_LAYOUT, 0)!!
        val showTopRated = prefs.getBoolean(SHOW_TOP_RATED, false)
        val showId = prefs.getInt(LAST_CLICKED_SHOW_ID, RECYCLER_SCROLL_DEFAULT_POSITION)
        showDetailsStateLiveData.value = ShowDetailsState(
            email, avatarUri, profileImageUrl, currentRecyclerViewType, showTopRated, showId
        )
    }

    /**
     * Return list of shows from
     */
    fun getShows(getTopRated: Boolean) {
        if (getTopRated) {
            getTopRatedShows()
        } else {
            if (networkChecker.internetIsConnected()) {
                getAllShowsFromApi()
            } else {
                getAllShowsFromDatabase()
            }
        }
    }

    private fun getAllShowsFromApi() {
        showsRepository.getAllShowsFromApi()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<AllShowsResponse> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: AllShowsResponse) {
                    allShowsLiveData.postValue(
                        t.shows.map { show ->
                            UiShow(
                                show.id,
                                show.averageRating,
                                show.description,
                                show.imageUrl,
                                show.noOfReviews,
                                show.title
                            )
                        }
                    )
                    showsRepository.saveShowsInDatabase(t)
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    allShowsLiveData.postValue(emptyList())
                }
            })
    }

    private fun getTopRatedShows() {
        showsRepository.getTopRatedShowsFromApi()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<TopRatedShowsResponse> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: TopRatedShowsResponse) {
                    allShowsLiveData.postValue(
                        t.shows.map { show ->
                            UiShow(
                                show.id,
                                show.averageRating,
                                show.description,
                                show.imageUrl,
                                show.noOfReviews,
                                show.title
                            )
                        }
                    )
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    allShowsLiveData.postValue(emptyList())
                }
            })
    }

    private fun getAllShowsFromDatabase() {
        showsRepository.getAllShowsFromDatabase()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<ShowEntity>> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: List<ShowEntity>) {
                    allShowsLiveData.postValue(
                        t.map { show ->
                            UiShow(
                                show.id,
                                show.averageRating,
                                show.description,
                                show.imageUrl,
                                show.noOfReviews,
                                show.title
                            )
                        }
                    )
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    allShowsLiveData.postValue(emptyList())
                }
            })
    }

    /**
     * Call a repository function for uploading image to server
     */
    fun uploadImage(file: File) {
        imageUploadRepository.uploadImage(file)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<UploadPictureResponse> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: UploadPictureResponse) {
                    imageUploadResultLiveData.postValue(t.user)
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    imageUploadResultLiveData.postValue(null)
                }
            })
    }

    fun saveShowIdToSharedPrefs(showId: Int) {
        prefs.edit {
            putInt(LAST_CLICKED_SHOW_ID, showId)
        }
        showDetailsStateLiveData.value = showDetailsStateLiveData.value?.copy(showId = showId)
    }

    fun setRecyclerViewLayoutInSharedPrefs(currentRecyclerViewType: Int) {
        prefs.edit {
            putInt(RECYCLER_VIEW_LAYOUT, currentRecyclerViewType)
        }
        showDetailsStateLiveData.value = showDetailsStateLiveData.value?.copy(
            currentRecyclerViewType = currentRecyclerViewType
        )
    }

    fun setTopRatedInSharedPrefs(showTopRated: Boolean) {
        prefs.edit {
            putBoolean(SHOW_TOP_RATED, showTopRated)
        }
        showDetailsStateLiveData.value = showDetailsStateLiveData.value?.copy(showTopRated = showTopRated)
    }

    fun setProfilePictureUriInSharedPrefs(avatarUri: Uri?) {
        prefs.edit {
            putString(URI, avatarUri.toString())
        }
        avatarUri?.let {
            showDetailsStateLiveData.value = showDetailsStateLiveData.value?.copy(avatarUri = it)
        }
    }

    fun setProfilePictureUrlInSharedPrefs(profileImageUrl: String?) {
        prefs.edit {
            putString(URL, profileImageUrl)
        }
        profileImageUrl?.let {
            showDetailsStateLiveData.value = showDetailsStateLiveData.value?.copy(profileImageUrl = it)
        }
    }

    fun logout() {
        prefs.edit {
            putBoolean(USER_LOGGED_IN, false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable != null)
            disposable.dispose()
    }
}

data class ShowDetailsState(
    val email: String,
    val avatarUri: Uri,
    val profileImageUrl: String,
    val currentRecyclerViewType: Int,
    val showTopRated: Boolean,
    val showId: Int
)
