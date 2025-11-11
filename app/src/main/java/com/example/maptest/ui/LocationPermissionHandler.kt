package com.example.maptest.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Handles runtime permission requests for accessing the user's fine location.
 *
 * This composable monitors the permission state for `ACCESS_FINE_LOCATION`
 * using Accompanist Permissions and reacts accordingly:
 *
 * - If permission is granted → `onPermissionGranted()` is called.
 * - If permission is denied once → shows rationale dialog (`showRationaleDialog`).
 * - If permission is permanently denied → shows settings dialog (`showSettingsDialog`).
 *
 * ## ⚠️ Behavior notes on Android 13 (API 33) and above
 *
 * Starting from **Android 13 (API 33)**, the system's permission dialog UX has changed:
 * - The **"Don't ask again"** checkbox is no longer displayed.
 * - The system now manages re-request behavior automatically.
 * - As a result, `shouldShowRationale` almost always returns `false`
 *   even after the user denies the permission.
 *
 * Therefore, on **API 33+**, `showRationaleDialog` is rarely triggered.
 * The flow typically goes straight from denial to `showSettingsDialog`.
 *
 * To test rationale dialog behavior, use an **emulator running Android 12L (API 32)** or lower.
 *
 * @param onPermissionGranted Composable callback executed when permission is granted.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> {
                showRationaleDialog = false
                showSettingsDialog = false
            }
            permissionState.status.shouldShowRationale -> {
                showRationaleDialog = true
            }
            else -> {
                showSettingsDialog = true
            }
        }
    }

    // 권한 요청 버튼
    if (!permissionState.status.isGranted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("위치 권한 요청")
            }
        }
    } else {
        onPermissionGranted()
    }

    if (showRationaleDialog) {
        PermissionDialog(
            title = "위치 권한이 필요합니다",
            message = "지도의 현 위치를 표시하려면 위치 접근 권한이 필요합니다.",
            confirmText = "다시 요청",
            onConfirm = {
                showRationaleDialog = false
                permissionState.launchPermissionRequest()
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    if (showSettingsDialog) {
        PermissionDialog(
            title = "권한 설정 필요",
            message = "설정에서 위치 권한을 직접 허용해야 합니다.",
            confirmText = "설정으로 이동",
            onConfirm = {
                showSettingsDialog = false
                openAppSettings(context)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun PermissionDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        }
    )
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}