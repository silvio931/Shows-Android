package com.silvio_mihalic.shows.di.modules

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides

@Module
class SharedPreferencesModule {

    @Provides
    fun preferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
    }
}
