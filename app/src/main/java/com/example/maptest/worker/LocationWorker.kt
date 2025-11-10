package com.example.maptest.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.maptest.data.LocationDao
import com.example.maptest.data.LocationEntity
import com.google.android.gms.location.LocationServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val locationDao: LocationDao
) : CoroutineWorker(context, workerParameters) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                val randomLat = Random.nextDouble(37.4, 37.7)
                val randomLng = Random.nextDouble(126.8, 127.2)
                locationDao.insert(
//                    LocationEntity(
//                        latitude = location.latitude,
//                        longitude = location.longitude
//                    )
                    LocationEntity(latitude = randomLat, longitude = randomLng)
                )
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
