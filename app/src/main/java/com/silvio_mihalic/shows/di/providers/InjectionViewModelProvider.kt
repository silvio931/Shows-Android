package com.silvio_mihalic.shows.di.providers

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Lazy
import javax.inject.Inject

class InjectionViewModelProvider<V> @Inject constructor(
    private val lazyViewModel: Lazy<V>
) {

    @Suppress("UNCHECKED_CAST")
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>) = lazyViewModel.get() as T
    }

    inline fun <reified V : ViewModel, reified F : Fragment> provide(fragment: F) =
        ViewModelProvider(fragment, factory).get(V::class.java)
}
