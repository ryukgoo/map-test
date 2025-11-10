package com.example.maptest.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapScreen() {
    val context = LocalContext.current

    // ✅ ViewModel 생성
    val viewModel = remember { MapViewModel(context) }

    // ✅ Room DB에서 최신 위치 Flow 관찰
    val latestLocation by viewModel.latestLocation.collectAsStateWithLifecycle()
    val currentLatLng = latestLocation?.let { LatLng(it.latitude, it.longitude) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showPermissionDeniedText by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        showPermissionDeniedText = !isGranted

        if (!isGranted && context is Activity) {
            permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    fun onRequestPermission() {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun onCurrentLocationClick() {
        if (hasPermission) {
            viewModel.requestLocation(context)
        } else {
            onRequestPermission()
        }
    }

    MapContent(
        currentLocation = currentLatLng,
        hasPermission = hasPermission,
        showPermissionDeniedText = showPermissionDeniedText,
        permanentlyDenied = permanentlyDenied,
        onRequestPermission = ::onRequestPermission,
        onCurrentLocationClick = ::onCurrentLocationClick
    )
}

@Composable
private fun MapContent(
    modifier: Modifier = Modifier,
    currentLocation: LatLng? = null,
    hasPermission: Boolean,
    showPermissionDeniedText: Boolean,
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
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

    if (hasPermission) {
        Box(modifier = modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
            ) {
                currentLocation?.let { Marker(state = markerState, title = "현재 위치") }
            }

            Button(
                onClick = {
                    onCurrentLocationClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("현 위치")
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showPermissionDeniedText) {
                Text("위치 권한이 필요합니다.")
            }
            if (permanentlyDenied) {
                Text("권한이 영구히 거부되었습니다. 설정에서 직접 허용해주세요.")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestPermission) {
                Text("권한 요청")
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