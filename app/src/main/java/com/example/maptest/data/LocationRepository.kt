package com.example.maptest.data

import androidx.lifecycle.asFlow
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.maptest.worker.LocationWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val workManager: WorkManager
) {

    companion object {
        const val LOCATION_UPDATE_WORK = "LOCATION_UPDATE_WORK"
    }

    fun getLatestLocation(): Flow<LocationEntity?> = locationDao.getLatestLocation()

    fun updateLocation(): UUID {
        val request = OneTimeWorkRequestBuilder<LocationWorker>().build()

        workManager.enqueueUniqueWork(
            LOCATION_UPDATE_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
        return request.id
    }

    fun observeWorkState(id: UUID?): Flow<String?> =
        id?.let {
            workManager.getWorkInfoByIdLiveData(id)
                .asFlow()
                .map { it?.progress?.getString("status") }
        } ?: run {
            flowOf(null)
        }
}