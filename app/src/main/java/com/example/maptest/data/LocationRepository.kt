package com.example.maptest.data

import androidx.lifecycle.asFlow
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.maptest.di.WorkKeys.LOCATION_UPDATE_WORK
import com.example.maptest.worker.LocationWorker
import com.example.maptest.worker.WorkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface LocationRepository {
    fun getLatestLocation(): Flow<LocationEntity?>
    fun updateLocation(): UUID
    fun observeWorkState(id: UUID?): Flow<WorkState>
}

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val workManager: WorkManager
) : LocationRepository {

    override fun getLatestLocation(): Flow<LocationEntity?> = locationDao.getLatestLocation()

    override fun updateLocation(): UUID {
        val request = OneTimeWorkRequestBuilder<LocationWorker>().build()

        workManager.enqueueUniqueWork(
            LOCATION_UPDATE_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
        return request.id
    }

    override fun observeWorkState(id: UUID?): Flow<WorkState> =
        id?.let {
            workManager.getWorkInfoByIdLiveData(id)
                .asFlow()
                .map { WorkState.from(it?.progress?.getString("status")) }
        } ?: flowOf(WorkState.UNKNOWN)
}