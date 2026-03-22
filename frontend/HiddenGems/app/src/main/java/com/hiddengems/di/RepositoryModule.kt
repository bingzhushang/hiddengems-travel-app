package com.hiddengems.di

import com.hiddengems.data.remote.ApiService
import com.hiddengems.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService): AuthRepository {
        return AuthRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideSpotRepository(apiService: ApiService): SpotRepository {
        return SpotRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideItineraryRepository(apiService: ApiService): ItineraryRepository {
        return ItineraryRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideAIRepository(apiService: ApiService): AIRepository {
        return AIRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideCommunityRepository(apiService: ApiService): CommunityRepository {
        return CommunityRepository(apiService)
    }
}
