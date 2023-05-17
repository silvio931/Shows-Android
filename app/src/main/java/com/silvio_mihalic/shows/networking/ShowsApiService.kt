package com.silvio_mihalic.shows.networking

import com.silvio_mihalic.shows.model.api.AllShowsResponse
import com.silvio_mihalic.shows.model.api.CreateReviewRequest
import com.silvio_mihalic.shows.model.api.CreateReviewResponse
import com.silvio_mihalic.shows.model.api.LoginRequest
import com.silvio_mihalic.shows.model.api.LoginResponse
import com.silvio_mihalic.shows.model.api.OneShowResponse
import com.silvio_mihalic.shows.model.api.RegisterRequest
import com.silvio_mihalic.shows.model.api.RegisterResponse
import com.silvio_mihalic.shows.model.api.ReviewsResponse
import com.silvio_mihalic.shows.model.api.TopRatedShowsResponse
import com.silvio_mihalic.shows.model.api.UploadPictureResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ShowsApiService {

    @POST("/users")
    fun register(@Body request: RegisterRequest): Single<Response<RegisterResponse>>

    @POST("/users/sign_in")
    fun login(@Body request: LoginRequest): Single<Response<LoginResponse>>

    @GET("/shows")
    fun getAllShows(): Single<AllShowsResponse>

    @GET("/shows/top_rated")
    fun getTopRatedShows(): Single<TopRatedShowsResponse>

    @GET("/shows/{id}")
    fun getOneShow(@Path("id") id: String): Single<OneShowResponse>

    @GET("/shows/{show_id}/reviews")
    fun getReviews(@Path("show_id") id: String): Single<ReviewsResponse>

    @POST("/reviews")
    fun createReview(@Body request: CreateReviewRequest): Call<CreateReviewResponse>

    @Multipart
    @PUT("/users")
    fun uploadImage(@Part image: MultipartBody.Part): Single<UploadPictureResponse>
}
