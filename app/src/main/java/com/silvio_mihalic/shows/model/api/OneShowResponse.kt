package com.silvio_mihalic.shows.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OneShowResponse(
    @SerialName("show") val show: Show
)
