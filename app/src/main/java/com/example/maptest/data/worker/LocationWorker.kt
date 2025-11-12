package com.example.maptest.data.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.maptest.data.database.LocationDao
import com.example.maptest.data.database.LocationEntity
import com.google.android.gms.location.LocationServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Represents the execution state of a background work task
 * managed by [androidx.work.WorkManager].
 *
 * This enum provides a simple mapping between string-based
 * progress updates and strongly-typed work states.
 */
enum class WorkState(val status: String) {

    /** Work has not yet started or is idle. */
    IDLE("IDLE"),

    /** Work is currently running in the background. */
    RUNNING("RUNNING"),

    /** Work has failed due to an unexpected error. */
    FAILED("FAILED"),

    /** Work has successfully completed. */
    DONE("DONE"),

    /** Unknown or undefined state (used as fallback). */
    UNKNOWN("UNKNOWN");

    companion object {
        fun from(status: String?): WorkState =
            when (status?.uppercase()) {
                RUNNING.status -> RUNNING
                DONE.status -> DONE
                FAILED.status -> FAILED
                IDLE.status -> IDLE
                else -> UNKNOWN
            }
    }
}

@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val locationDao: LocationDao
) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val KEY_STATUS = "status"
    }

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setProgress(workDataOf(KEY_STATUS to WorkState.RUNNING.status))
//            delay(1000L) // Simulate long-running task

            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
//                val randomLat = Random.nextDouble(37.4, 37.7)
//                val randomLng = Random.nextDouble(126.8, 127.2)
                locationDao.insert(
                    LocationEntity(latitude = location.latitude, longitude = location.longitude)
//                    LocationEntity(latitude = randomLat, longitude = randomLng)
                )
                setProgress(workDataOf(KEY_STATUS to WorkState.DONE.status))
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            setProgress(workDataOf(KEY_STATUS to WorkState.FAILED.status))
            Result.failure()
        }
    }
}
