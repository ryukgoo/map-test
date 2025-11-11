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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val workState by viewModel.workState.collectAsStateWithLifecycle()

    LocationPermissionHandler {
        MapContent(
            currentLocation = currentLocation,
            workState = workState,
            onCurrentLocationClick = viewModel::updateLocation
        )
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    currentLocation: LatLng? = null,
    workState: String? = null,
    onCurrentLocationClick: () -> Unit
) {
    val location = currentLocation ?: LatLng(37.5665, 126.9780)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 12f)
    }
    val markerState = rememberUpdatedMarkerState(location)

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 12f),
                durationMs = 500
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            currentLocation?.let {
                Marker(state = markerState, title = "현재 위치")
            }
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
                    "RUNNING" -> { // ✅ 위치 갱신 중
                        Spacer(modifier = Modifier.size(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    "FAILED" -> { // ✅ 갱신 실패
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    else -> {} // IDLE / SUCCEEDED 시 인디케이터 없음
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