package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivitiesModule {

    @ContributesAndroidInjector
    fun mainActivity(): MainActivity
}
