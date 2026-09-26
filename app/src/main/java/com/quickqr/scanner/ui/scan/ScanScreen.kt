package com.quickqr.scanner.ui.scan

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.quickqr.scanner.R
import com.quickqr.scanner.camera.CameraPreview
import com.quickqr.scanner.camera.QrCodeAnalyzer
import com.quickqr.scanner.ui.components.ViewfinderOverlay
import com.quickqr.scanner.ui.theme.OnScan
import com.quickqr.scanner.ui.theme.ScanBackground
import com.quickqr.scanner.ui.theme.SurfaceCard
import com.quickqr.scanner.util.ScanHistoryStore

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onQrScanned: (String) -> Unit,
    onOpenHistoryItem: (String) -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val historyStore = remember { ScanHistoryStore(context) }
    var history by remember { mutableStateOf(historyStore.getAll()) }
    var showHistory by remember { mutableStateOf(false) }

    val analyzer = remember {
        QrCodeAnalyzer { value ->
            lightVibrate(context)
            historyStore.add(value)
            onQrScanned(value)
        }
    }

    // Reset lock when returning to this screen
    DisposableEffect(Unit) {
        analyzer.reset()
        history = historyStore.getAll()
        onDispose { }
    }

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScanBackground)
    ) {
        when {
            permissionState.status.isGranted -> {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    analyzer = analyzer
                )
                ViewfinderOverlay(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = OnScan,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = OnScan,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        TextButton(onClick = {
                            history = historyStore.getAll()
                            showHistory = !showHistory
                        }) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = OnScan)
                            Text(
                                text = stringResource(R.string.history_title),
                                color = OnScan,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.scan_hint),
                    color = OnScan.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                )

                if (showHistory) {
                    HistorySheet(
                        items = history,
                        onClear = {
                            historyStore.clear()
                            history = emptyList()
                        },
                        onSelect = { onOpenHistoryItem(it) },
                        onDismiss = { showHistory = false },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }

            permissionState.status.shouldShowRationale -> {
                PermissionRationale(
                    onGrant = { permissionState.launchPermissionRequest() }
                )
            }

            else -> {
                PermissionRationale(
                    permanentlyDenied = true,
                    onGrant = { permissionState.launchPermissionRequest() },
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    permanentlyDenied: Boolean = false,
    onGrant: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.QrCodeScanner,
            contentDescription = null,
            tint = OnScan,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.camera_permission_rationale),
            color = OnScan,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGrant) {
            Text(stringResource(R.string.grant_camera))
        }
        if (permanentlyDenied && onOpenSettings != null) {
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        }
    }
}

@Composable
private fun HistorySheet(
    items: List<String>,
    onClear: () -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = SurfaceCard,
        tonalElevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.history_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnScan
                )
                Row {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text(stringResource(R.string.clear_history))
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
            if (items.isEmpty()) {
                Text(
                    stringResource(R.string.history_empty),
                    color = OnScan.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items) { item ->
                        Text(
                            text = item,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = OnScan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(item) }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun lightVibrate(context: android.content.Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService<VibratorManager>() ?: return
            vm.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = context.getSystemService<Vibrator>() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(40)
            }
        }
    } catch (_: SecurityException) {
        // VIBRATE optional
    }
}
