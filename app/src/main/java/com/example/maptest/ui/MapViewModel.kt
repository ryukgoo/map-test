package com.example.maptest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maptest.data.LocationRepository
import com.example.maptest.worker.WorkState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private var currentWorkId: UUID? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLatestLocation()
                .map { it?.let { LatLng(it.latitude, it.longitude) } }
                .collect { location ->
                    withContext(Dispatchers.Main) {
                        location?.let {
                            _uiState.update { state -> state.copy(location = it) }
                        }
                    }
                }
        }
    }

    fun updateLocation() {
        currentWorkId = repository.updateLocation()

        viewModelScope.launch(Dispatchers.IO) {
            repository.observeWorkState(currentWorkId)
                .collectLatest { status ->
                    withContext(Dispatchers.Main) {
                        _uiState.update { state -> state.copy(workState = status) }
                    }
                }
        }
    }
}

data class MapUiState(
    val location: LatLng = LatLng(37.5665, 126.9780),
    val workState: WorkState = WorkState.IDLE
)