package com.silvio_mihalic.shows.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Show(
    @SerialName("id") val id: String,
    @SerialName("average_rating") val averageRating: Int,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("no_of_reviews") val noOfReviews: Int,
    @SerialName("title") val title: String
)
