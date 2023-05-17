package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.di.providers.InjectionViewModelProvider
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.ui.login.LoginFragment
import com.silvio_mihalic.shows.ui.login.LoginViewModel
import dagger.Module
import dagger.Provides

@Module
class LoginModule {

    @Provides
    @ViewModelInjection
    fun viewModel(
        provider: InjectionViewModelProvider<LoginViewModel>,
        fragment: LoginFragment
    ): LoginViewModel = provider.provide(fragment)
}
