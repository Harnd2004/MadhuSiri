package com.example.madhusiri.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.madhusiri.ui.theme.*
import com.example.madhusiri.ui.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HiveMapScreen(navController: NavHostController, viewModel: MainViewModel) {
    val hives   by viewModel.hives.collectAsState()
    val alerts  by viewModel.sprayAlerts.collectAsState()
    val user    by viewModel.userProfile.collectAsState()
    val locPerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var showAddHiveDialog by remember { mutableStateOf(false) }
    var selectedLatLng    by remember { mutableStateOf<LatLng?>(null) }

    val defaultLocation = LatLng(12.9716, 77.5946)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    LaunchedEffect(Unit) {
        if (!locPerm.status.isGranted) locPerm.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ Hive Map") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HoneyGold,   // ← was HoneyAmber
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (user?.role == "beekeeper") {
                FloatingActionButton(
                    onClick = { showAddHiveDialog = true },
                    containerColor = HoneyGold    // ← was HoneyAmber
                ) { Icon(Icons.Default.Add, "Add Hive", tint = Color.White) }
            }
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(padding),
            cameraPositionState = cameraState,
            onMapLongClick = { latLng ->
                if (user?.role == "beekeeper") {
                    selectedLatLng = latLng
                    showAddHiveDialog = true
                }
            }
        ) {
            // Hive markers — yellow pins
            hives.forEach { hive ->
                Marker(
                    state   = MarkerState(LatLng(hive.latitude, hive.longitude)),
                    title   = hive.name,
                    snippet = "Health: ${hive.healthStatus} | Honey: ${hive.honeyProduction}kg",
                    icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                )
            }

            // Spray alert circles — red danger zones
            alerts.forEach { alert ->
                val center = LatLng(alert.latitude, alert.longitude)
                Circle(
                    center      = center,
                    radius      = alert.radius,
                    fillColor   = Color(0x33FF0000),
                    strokeColor = Color(0xFFFF0000),
                    strokeWidth = 2f
                )
                Marker(
                    state   = MarkerState(center),
                    title   = "⚠️ Spray Alert",
                    snippet = "${alert.farmerName}: ${alert.pesticide}",
                    icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
        }

        if (showAddHiveDialog) {
            AddHiveDialog(
                onDismiss = { showAddHiveDialog = false; selectedLatLng = null },
                onConfirm = { name ->
                    val loc = selectedLatLng ?: cameraState.position.target
                    viewModel.addHive(name, loc.latitude, loc.longitude)
                    showAddHiveDialog = false
                    selectedLatLng = null
                }
            )
        }
    }
}

@Composable
fun AddHiveDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("📍 Add Hive Location") },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Hive Name") },
                placeholder = { Text("e.g. North Field Hive") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = HoneyGold),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Add Hive") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}