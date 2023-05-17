package com.silvio_mihalic.shows.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.silvio_mihalic.shows.model.database.ReviewEntity
import com.silvio_mihalic.shows.model.database.ReviewEntityOffline
import com.silvio_mihalic.shows.model.database.ShowEntity

@Database(
    entities = [
        ShowEntity::class,
        ReviewEntity::class,
        ReviewEntityOffline::class
    ],
    version = 1
)
abstract class ShowsDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
    abstract fun reviewDao(): ReviewDao
    abstract fun reviewOfflineDao(): ReviewOfflineDao
}
