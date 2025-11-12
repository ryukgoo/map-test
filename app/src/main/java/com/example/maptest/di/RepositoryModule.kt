package com.example.maptest.di

import com.example.maptest.data.LocationRepository
import com.example.maptest.data.LocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository
}