package com.example.maptest.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.maptest.data.AppDatabaseProvider
import com.example.maptest.data.entity.LocationEntity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                val dao = AppDatabaseProvider.getLocationDao(applicationContext)
                dao.insert(
                    LocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
                Log.d("LocationWorker", "DB insert 완료: ${location.latitude}, ${location.longitude}")
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}