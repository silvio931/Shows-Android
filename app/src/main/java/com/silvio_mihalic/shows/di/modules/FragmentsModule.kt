package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.ui.SplashFragment
import com.silvio_mihalic.shows.ui.login.LoginFragment
import com.silvio_mihalic.shows.ui.registration.RegistrationFragment
import com.silvio_mihalic.shows.ui.showDetails.ShowDetailsFragment
import com.silvio_mihalic.shows.ui.shows.ShowsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface FragmentsModule {

    @ContributesAndroidInjector(modules = [LoginModule::class])
    fun loginFragment(): LoginFragment

    @ContributesAndroidInjector(modules = [ShowsModule::class])
    fun showsFragment(): ShowsFragment

    @ContributesAndroidInjector
    fun splashFragment(): SplashFragment

    @ContributesAndroidInjector(modules = [RegistrationModule::class])
    fun registrationFragment(): RegistrationFragment

    @ContributesAndroidInjector(modules = [ShowDetailsModule::class])
    fun showDetailsFragment(): ShowDetailsFragment
}
