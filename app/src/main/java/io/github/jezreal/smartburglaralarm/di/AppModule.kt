package io.github.jezreal.smartburglaralarm.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jezreal.smartburglaralarm.network.LedApi
import io.github.jezreal.smartburglaralarm.network.NodeApi
import io.github.jezreal.smartburglaralarm.repository.NodeRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTestApi(): LedApi {
        return Retrofit.Builder()
            .baseUrl(LedApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LedApi::class.java)
    }

    @Singleton
    @Provides
    fun provideNodeApi(): NodeApi {
        return Retrofit.Builder()
            .baseUrl(NodeApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(NodeApi::class.java)
    }

    @Singleton
    @Provides
    fun provideNodeRepository(api: NodeApi): NodeRepository {
        return NodeRepository(api)
    }

}