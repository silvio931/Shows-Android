package com.silvio_mihalic.shows.di.modules

import com.silvio_mihalic.shows.repository.ImageUploadRepository
import com.silvio_mihalic.shows.repository.ImageUploadRepositoryImpl
import com.silvio_mihalic.shows.repository.LoginRepository
import com.silvio_mihalic.shows.repository.LoginRepositoryImpl
import com.silvio_mihalic.shows.repository.RegistrationRepository
import com.silvio_mihalic.shows.repository.RegistrationRepositoryImpl
import com.silvio_mihalic.shows.repository.ReviewsRepository
import com.silvio_mihalic.shows.repository.ReviewsRepositoryImpl
import com.silvio_mihalic.shows.repository.ShowDetailsRepository
import com.silvio_mihalic.shows.repository.ShowDetailsRepositoryImpl
import com.silvio_mihalic.shows.repository.ShowsRepository
import com.silvio_mihalic.shows.repository.ShowsRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {

    @Binds
    fun login(repository: LoginRepositoryImpl): LoginRepository

    @Binds
    fun shows(repository: ShowsRepositoryImpl): ShowsRepository

    @Binds
    fun imageUpload(repository: ImageUploadRepositoryImpl): ImageUploadRepository

    @Binds
    fun registration(repository: RegistrationRepositoryImpl): RegistrationRepository

    @Binds
    fun showDetails(repository: ShowDetailsRepositoryImpl): ShowDetailsRepository

    @Binds
    fun reviews(repository: ReviewsRepositoryImpl): ReviewsRepository
}
