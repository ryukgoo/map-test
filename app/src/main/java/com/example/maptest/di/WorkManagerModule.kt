package com.example.maptest.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun providerWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

object WorkKeys {
    const val LOCATION_UPDATE_WORK = "LOCATION_UPDATE_WORK"
}