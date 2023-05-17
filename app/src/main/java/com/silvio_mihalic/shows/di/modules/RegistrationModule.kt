package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.di.providers.InjectionViewModelProvider
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.ui.registration.RegistrationFragment
import com.silvio_mihalic.shows.ui.registration.RegistrationViewModel
import dagger.Module
import dagger.Provides

@Module
class RegistrationModule {

    @Provides
    @ViewModelInjection
    fun viewModel(
        provider: InjectionViewModelProvider<RegistrationViewModel>,
        fragment: RegistrationFragment
    ): RegistrationViewModel = provider.provide(fragment)
}
