package com.silvio_mihalic.shows.di.modules

import android.content.Context
import com.silvio_mihalic.shows.ShowsApp
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerApplication
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: ShowsApp) {

    @Provides
    @Singleton
    fun application(): DaggerApplication = application

    @Provides
    @Singleton
    fun applicationContext(application: DaggerApplication): Context = application.applicationContext
}
