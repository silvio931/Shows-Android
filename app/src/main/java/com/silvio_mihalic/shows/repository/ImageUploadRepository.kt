package com.silvio_mihalic.shows.repository

import com.silvio_mihalic.shows.model.api.UploadPictureResponse
import com.silvio_mihalic.shows.networking.ShowsApiService
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

interface ImageUploadRepository {
    fun uploadImage(file: File): Single<UploadPictureResponse>
}

class ImageUploadRepositoryImpl @Inject constructor(
    private val apiService: ShowsApiService
) : ImageUploadRepository {

    /**
     * Upload image given in file parameter to server
     */
    override fun uploadImage(file: File): Single<UploadPictureResponse> {
        val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)

        return apiService.uploadImage(body)
    }
}
