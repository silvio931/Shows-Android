package com.silvio_mihalic.shows.repository

import com.silvio_mihalic.shows.model.api.RegisterRequest
import com.silvio_mihalic.shows.model.api.RegisterResponse
import com.silvio_mihalic.shows.networking.ShowsApiService
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

interface RegistrationRepository {
    fun register(registerRequest: RegisterRequest): Single<Response<RegisterResponse>>
}

class RegistrationRepositoryImpl @Inject constructor(
    private val apiService: ShowsApiService
) : RegistrationRepository {

    override fun register(registerRequest: RegisterRequest): Single<Response<RegisterResponse>> {
        return apiService.register(registerRequest)
    }
}
