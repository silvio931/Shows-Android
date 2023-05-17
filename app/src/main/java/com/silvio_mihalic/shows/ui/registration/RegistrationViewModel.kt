package com.silvio_mihalic.shows.ui.registration

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.silvio_mihalic.shows.model.api.RegisterRequest
import com.silvio_mihalic.shows.model.api.RegisterResponse
import com.silvio_mihalic.shows.repository.RegistrationRepository
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class RegistrationViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    companion object {
        private const val REGISTRATION_SUCCESSFUL = "registrationSuccessful"
    }

    val disposable = CompositeDisposable()

    val registerResultLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun register(email: String, password: String, repeatedPassword: String) {
        registrationRepository.register(RegisterRequest(email, password, repeatedPassword))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Response<RegisterResponse>> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: Response<RegisterResponse>) {
                    registerResultLiveData.postValue(t.isSuccessful)
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    registerResultLiveData.postValue(false)
                }
            })
    }

    fun setRegistrationSuccessfulInSharedPrefs() {
        prefs.edit {
            putBoolean(REGISTRATION_SUCCESSFUL, true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable != null)
            disposable.dispose()
    }
}
