package com.example.maptest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maptest.data.LocationRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {

    private val defaultLocation = LatLng(37.5665, 126.9780)

    val currentLocation: StateFlow<LatLng> =
        repository.getLatestLocation()
            .map { it?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultLocation)

    private val _workState = MutableStateFlow<String?>(null)
    val workState: StateFlow<String?> = _workState

    private var currentWorkId: UUID? = null

    fun updateLocation() {
        currentWorkId = repository.updateLocation()

        viewModelScope.launch {
            repository.observeWorkState(currentWorkId)
                .collect { status -> _workState.update { status } }
        }
    }
}