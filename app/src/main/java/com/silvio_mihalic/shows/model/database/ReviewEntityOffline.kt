package com.silvio_mihalic.shows.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_offline")
data class ReviewEntityOffline(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "comment") val comment: String,
    @ColumnInfo(name = "rating") val rating: Int,
    @ColumnInfo(name = "show_id", index = true) val showId: Int,
    @ColumnInfo(name = "user_email") val userEmail: String
)
