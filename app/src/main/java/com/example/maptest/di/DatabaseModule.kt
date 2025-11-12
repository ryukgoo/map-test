package com.example.maptest.di

import android.content.Context
import androidx.room.Room
import com.example.maptest.data.database.AppDatabase
import com.example.maptest.data.database.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "location_database"
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideLocationDao(database: AppDatabase): LocationDao = database.locationDao()

}