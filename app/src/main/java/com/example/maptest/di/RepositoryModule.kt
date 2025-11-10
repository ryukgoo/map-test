package com.example.maptest.di

import androidx.work.WorkManager
import com.example.maptest.data.LocationDao
import com.example.maptest.data.LocationRepository
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
    fun provideLocationRepository(
        locationDao: LocationDao,
        workManager: WorkManager
    ): LocationRepository {
        return LocationRepository(locationDao, workManager)
    }
}