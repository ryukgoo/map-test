package com.example.maptest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maptest.data.repository.LocationRepository
import com.example.maptest.data.worker.WorkState
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

/**
 * UI state holder for the Map screen.
 *
 * This state combines both the user's latest known location
 * and the current background work status from [androidx.work.WorkManager].
 *
 * It is observed by the Compose UI layer to render the current
 * map marker position and update indicators (e.g., loading spinner, error icon).
 *
 * @property location The latest known geographic coordinate to display on the map.
 *                    Defaults to Seoul City Hall (37.5665, 126.9780).
 * @property workState The current status of the background work task handled by [androidx.work.WorkManager].
 *                     Used to indicate whether the location is being updated, failed, or completed.
 */
data class MapUiState(
    val location: LatLng = LatLng(37.5665, 126.9780),
    val workState: WorkState = WorkState.IDLE
)