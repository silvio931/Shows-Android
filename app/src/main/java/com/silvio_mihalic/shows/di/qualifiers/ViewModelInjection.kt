package com.silvio_mihalic.shows.di.qualifiers

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class ViewModelInjection
