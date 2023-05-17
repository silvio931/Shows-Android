package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.di.providers.InjectionViewModelProvider
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.ui.showDetails.ShowDetailsFragment
import com.silvio_mihalic.shows.ui.showDetails.ShowDetailsViewModel
import dagger.Module
import dagger.Provides

@Module
class ShowDetailsModule {

    @Provides
    @ViewModelInjection
    fun viewModel(
        provider: InjectionViewModelProvider<ShowDetailsViewModel>,
        fragment: ShowDetailsFragment
    ): ShowDetailsViewModel = provider.provide(fragment)
}
