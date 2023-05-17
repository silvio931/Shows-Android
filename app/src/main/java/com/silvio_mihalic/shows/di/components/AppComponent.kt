package com.silvio_mihalic.shows.di.components

import com.silvio_mihalic.shows.ShowsApp
import com.silvio_mihalic.shows.di.modules.ApiDaggerModule
import com.silvio_mihalic.shows.di.modules.ApplicationModule
import com.silvio_mihalic.shows.di.modules.FragmentsModule
import com.silvio_mihalic.shows.di.modules.RepositoryModule
import com.silvio_mihalic.shows.di.modules.RoomModule
import com.silvio_mihalic.shows.di.modules.SharedPreferencesModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        SharedPreferencesModule::class,
        ApiDaggerModule::class,
        FragmentsModule::class,
        RepositoryModule::class,
        RoomModule::class
    ]
)
@Singleton
interface AppComponent : AndroidInjector<ShowsApp> {

    @Component.Builder
    interface Builder {

        fun applicationModule(applicationModule: ApplicationModule): Builder

        fun build(): AppComponent
    }
}
