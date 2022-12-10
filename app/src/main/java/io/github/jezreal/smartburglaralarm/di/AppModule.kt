package io.github.jezreal.smartburglaralarm.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jezreal.smartburglaralarm.network.QuoteApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTestApi(): QuoteApi {
        return Retrofit.Builder()
            .baseUrl(QuoteApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(QuoteApi::class.java)
    }
}