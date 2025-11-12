package com.example.maptest.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maptest.data.worker.WorkState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.CancellationException

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LocationPermissionHandler {
        MapContent(
            currentLocation = uiState.location,
            workState = uiState.workState,
            onCurrentLocationClick = viewModel::updateLocation
        )
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    currentLocation: LatLng,
    workState: WorkState = WorkState.IDLE,
    onCurrentLocationClick: () -> Unit
) {
    val location = currentLocation
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 12f)
    }
    val markerState = rememberUpdatedMarkerState(location)

    LaunchedEffect(currentLocation) {
        try {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLocation, 12f),
                durationMs = 500
            )
        } catch (_: CancellationException) {
            // TODO: Handle cancellation if needed
//            cameraPositionState.move(
//                CameraUpdateFactory.newLatLngZoom(currentLocation, 12f)
//            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            Marker(state = markerState, title = "현재 위치")
        }

        Button(
            onClick = onCurrentLocationClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("현 위치")

                when (workState) {
                    WorkState.RUNNING -> { // ✅ 위치 갱신 중
                        Spacer(modifier = Modifier.size(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }

                    WorkState.FAILED -> { // ✅ 갱신 실패
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "MapScreen Preview",
    widthDp = 360,
    heightDp = 720,
)
@Composable
private fun MapScreenPreview() {
    MapScreen()
}