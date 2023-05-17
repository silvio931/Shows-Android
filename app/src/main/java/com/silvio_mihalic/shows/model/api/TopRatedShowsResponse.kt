package com.silvio_mihalic.shows.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopRatedShowsResponse(
    @SerialName("shows") val shows: List<Show>
)
