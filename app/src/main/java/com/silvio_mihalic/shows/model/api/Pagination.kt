package com.silvio_mihalic.shows.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    @SerialName("count") val count: Int,
    @SerialName("page") val page: Int,
    @SerialName("items") val items: Int,
    @SerialName("pages") val pages: Int
)
