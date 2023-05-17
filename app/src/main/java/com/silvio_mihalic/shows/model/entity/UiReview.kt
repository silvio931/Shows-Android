package com.silvio_mihalic.shows.model.entity

class UiReview(
    val id: String,
    val comment: String,
    val rating: Int,
    val showId: Int,
    val userEmail: String,
    val userImageUrl: String?
)
