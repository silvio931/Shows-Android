package com.silvio_mihalic.shows.repository

import android.content.SharedPreferences
import com.silvio_mihalic.shows.model.api.LoginRequest
import com.silvio_mihalic.shows.model.api.LoginResponse
import com.silvio_mihalic.shows.networking.ShowsApiService
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

interface LoginRepository {
    fun login(loginRequest: LoginRequest): Single<Response<LoginResponse>>
    fun saveValuesInSharedPrefs(response: Response<LoginResponse>)
}

class LoginRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences,
    private val apiService: ShowsApiService
) : LoginRepository {

    companion object {
        private const val ACCESS_TOKEN_HEADER = "access-token"
        private const val CLIENT_HEADER = "client"
        private const val UID_HEADER = "uid"
        private const val URL = "url"
    }

    /**
     * Calls login api, save header values in shared prefs if login successful
     */
    override fun login(loginRequest: LoginRequest): Single<Response<LoginResponse>> {
        return apiService.login(loginRequest)
    }

    override fun saveValuesInSharedPrefs(response: Response<LoginResponse>) {
        with(preferences.edit()) {
            putString(ACCESS_TOKEN_HEADER, response.headers().get(ACCESS_TOKEN_HEADER))
            putString(CLIENT_HEADER, response.headers().get(CLIENT_HEADER))
            putString(UID_HEADER, response.headers().get(UID_HEADER))
            putString(URL, response.body()?.user?.imageUrl)
            apply()
        }
    }
}
