package com.silvio_mihalic.shows.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "review"
)
data class ReviewEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "comment") val comment: String,
    @ColumnInfo(name = "rating") val rating: Int,
    @ColumnInfo(name = "show_id", index = true) val showId: Int,
    @ColumnInfo(name = "user_email") val userEmail: String,
    @ColumnInfo(name = "user_image_url") val userImageUrl: String?
)
