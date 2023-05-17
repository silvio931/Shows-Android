package com.silvio_mihalic.shows

import com.silvio_mihalic.shows.di.components.DaggerAppComponent
import com.silvio_mihalic.shows.di.modules.ApplicationModule
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class ShowsApp : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<ShowsApp> {
        return DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}
