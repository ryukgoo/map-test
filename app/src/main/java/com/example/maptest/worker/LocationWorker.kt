package com.example.maptest.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.maptest.data.LocationDao
import com.example.maptest.data.LocationEntity
import com.google.android.gms.location.LocationServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class WorkState(val status: String) {
    IDLE("IDLE"),
    RUNNING("RUNNING"),
    FAILED("FAILED"),
    DONE("DONE"),
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

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setProgress(workDataOf("status" to WorkState.RUNNING.status))
            delay(1000L) // Simulate long-running task

            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                val randomLat = Random.nextDouble(37.4, 37.7)
                val randomLng = Random.nextDouble(126.8, 127.2)
                locationDao.insert(
//                    LocationEntity(latitude = location.latitude, longitude = location.longitude)
                    LocationEntity(latitude = randomLat, longitude = randomLng)
                )
                setProgress(workDataOf("status" to WorkState.DONE.status))
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            setProgress(workDataOf("status" to WorkState.FAILED.status))
            Result.failure()
        }
    }
}
