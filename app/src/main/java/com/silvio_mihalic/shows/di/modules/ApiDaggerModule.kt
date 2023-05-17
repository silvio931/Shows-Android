package com.silvio_mihalic.shows.di.modules

import android.content.SharedPreferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.silvio_mihalic.shows.networking.ShowsApiService
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

@Module
class ApiDaggerModule {
    companion object {
        private const val BASE_URL = "" // API not availables
        private const val TOKEN_TYPE_HEADER = "token-type"
        private const val TOKEN_TYPE_VALUE = "Bearer"
        private const val ACCESS_TOKEN_HEADER = "access-token"
        private const val CLIENT_HEADER = "client"
        private const val UID_HEADER = "uid"
    }

    @Provides
    fun apiService(client: OkHttpClient): ShowsApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ShowsApiService::class.java)
    }

    @Provides
    fun okHttpClient(preferences: SharedPreferences): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .addInterceptor { chain ->
                val original = chain.request()

                val requestBuilder = original.newBuilder()
                    .header(TOKEN_TYPE_HEADER, TOKEN_TYPE_VALUE)
                    .header(ACCESS_TOKEN_HEADER, preferences.getString(ACCESS_TOKEN_HEADER, null).toString())
                    .header(CLIENT_HEADER, preferences.getString(CLIENT_HEADER, null).toString())
                    .header(UID_HEADER, preferences.getString(UID_HEADER, null).toString())

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }
}
