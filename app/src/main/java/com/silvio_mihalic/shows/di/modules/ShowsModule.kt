package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.di.providers.InjectionViewModelProvider
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.ui.shows.ShowsFragment
import com.silvio_mihalic.shows.ui.shows.ShowsViewModel
import dagger.Module
import dagger.Provides

@Module
class ShowsModule {

    @Provides
    @ViewModelInjection
    fun viewModel(
        provider: InjectionViewModelProvider<ShowsViewModel>,
        fragment: ShowsFragment
    ): ShowsViewModel = provider.provide(fragment)
}
