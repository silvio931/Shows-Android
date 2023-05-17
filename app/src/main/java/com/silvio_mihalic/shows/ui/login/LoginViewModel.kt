package com.silvio_mihalic.shows.ui.login

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.silvio_mihalic.shows.model.api.LoginRequest
import com.silvio_mihalic.shows.model.api.LoginResponse
import com.silvio_mihalic.shows.repository.LoginRepository
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    companion object {
        private const val REGISTRATION_SUCCESSFUL = "registrationSuccessful"
        private const val USER_LOGGED_IN = "loggedIn"
        private const val EMAIL = "email"
    }

    val cameFromRegistrationLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val loginResultLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val disposable = CompositeDisposable()

    init {
        cameFromRegistrationLiveData.value = prefs.getBoolean(REGISTRATION_SUCCESSFUL, false)
    }

    fun login(email: String, password: String) {
        loginRepository.login(LoginRequest(email, password))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Response<LoginResponse>> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onSuccess(t: Response<LoginResponse>) {
                    loginRepository.saveValuesInSharedPrefs(t)
                    loginResultLiveData.postValue(t.isSuccessful)
                }

                override fun onError(e: Throwable) {
                    Log.d("error", e.toString())
                    loginResultLiveData.postValue(false)
                }
            })
    }

    fun setRegistrationSuccessfulPrefs() {
        prefs.edit {
            putBoolean(REGISTRATION_SUCCESSFUL, false)
        }
        cameFromRegistrationLiveData.value = false
    }

    fun saveEmailAndUserLoggedInSharedPrefs(email: String, rememberMe: Boolean) {
        prefs.edit {
            putString(EMAIL, email)
            if (rememberMe) {
                putBoolean(USER_LOGGED_IN, true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable != null)
            disposable.dispose()
    }
}
