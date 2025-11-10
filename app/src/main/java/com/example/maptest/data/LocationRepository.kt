package com.example.maptest.data

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.maptest.worker.LocationWorker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val workManager: WorkManager
) {
    fun getLatestLocation(): Flow<LocationEntity?> = locationDao.getLatestLocation()

    fun updateLocation() {
        val request = OneTimeWorkRequestBuilder<LocationWorker>().build()
        workManager.enqueue(request)
    }
}