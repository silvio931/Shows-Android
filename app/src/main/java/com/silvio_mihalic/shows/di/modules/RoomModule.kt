package com.silvio_mihalic.shows.di.modules

import android.content.Context
import androidx.room.Room
import com.silvio_mihalic.shows.database.ShowsDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {

    @Provides
    @Singleton
    fun database(context: Context) =
        Room.databaseBuilder(
            context,
            ShowsDatabase::class.java,
            "shows_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun showDao(database: ShowsDatabase) = database.showDao()

    @Provides
    fun reviewDao(database: ShowsDatabase) = database.reviewDao()

    @Provides
    fun reviewDaoOffline(database: ShowsDatabase) = database.reviewOfflineDao()
}
