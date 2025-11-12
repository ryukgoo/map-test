package com.example.maptest.di

import com.example.maptest.data.repository.LocationRepository
import com.example.maptest.data.repository.LocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository
}