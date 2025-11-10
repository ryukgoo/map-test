package com.example.maptest.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.maptest.data.AppDatabaseProvider
import com.example.maptest.worker.LocationWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MapViewModel(context: Context): ViewModel() {
    private val dao = AppDatabaseProvider.getLocationDao(context)

    val latestLocation = dao.getLatestLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun requestLocation(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<LocationWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}