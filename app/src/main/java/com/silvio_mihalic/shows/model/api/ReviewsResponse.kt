package com.silvio_mihalic.shows.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewsResponse(
    @SerialName("reviews") val reviews: List<Review>,
    @SerialName("meta") val meta: Meta
)
