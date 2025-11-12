package com.example.maptest.data.repository

import androidx.lifecycle.asFlow
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.maptest.data.database.LocationDao
import com.example.maptest.data.database.LocationEntity
import com.example.maptest.di.WorkKeys.LOCATION_UPDATE_WORK
import com.example.maptest.data.worker.LocationWorker
import com.example.maptest.data.worker.WorkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface that defines data access and background update operations
 * for managing the user's location information.
 *
 * This repository abstracts the interaction between the local database (Room)
 * and WorkManager, providing a unified API for fetching and updating location data.
 */
interface LocationRepository {

    /**
     * Retrieves the latest stored location from the local database.
     *
     * @return [Flow] that emits the most recent [LocationEntity] or `null` if none exists.
     */
    fun getLatestLocation(): Flow<LocationEntity?>

    /**
     * Requests an asynchronous update of the user's current location.
     * The update is handled by [WorkManager] through a [LocationWorker].
     *
     * @return The unique [UUID] identifying the enqueued WorkManager task.
     */
    fun updateLocation(): UUID

    /**
     * Observes the current state of a location update task.
     *
     * @param id The unique [UUID] of the work request to observe.
     * @return [Flow] that emits the current [WorkState] for the specified task,
     * or [WorkState.UNKNOWN] if the ID is `null` or unavailable.
     */
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
                .map {
                    WorkState.from(it?.progress?.getString(LocationWorker.KEY_STATUS))
                }
        } ?: flowOf(WorkState.UNKNOWN)
}