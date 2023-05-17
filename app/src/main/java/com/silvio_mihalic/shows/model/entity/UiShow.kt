package com.silvio_mihalic.shows.model.entity

data class UiShow(
    val id: String,
    val averageRating: Int,
    val description: String,
    val imageUrl: String,
    val noOfReviews: Int,
    val title: String
)
