package com.example.madhusiri.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.madhusiri.data.models.SprayAlert
import com.example.madhusiri.ui.theme.*
import com.example.madhusiri.ui.viewmodel.MainViewModel
import com.example.madhusiri.utils.LocationHelper
import com.example.madhusiri.utils.formatTimestamp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SprayAlertScreen(navController: NavHostController, viewModel: MainViewModel) {
    val user    by viewModel.userProfile.collectAsState()
    val alerts  by viewModel.sprayAlerts.collectAsState()
    val context = LocalContext.current

    var pesticide         by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isFetching        by remember { mutableStateOf(false) }
    var locationError     by remember { mutableStateOf<String?>(null) }
    val locPerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    fun postAlert() {
        isFetching = true; locationError = null
        LocationHelper(context).getCurrentLocation(
            onResult = { lat, lng ->
                isFetching = false
                viewModel.postSprayAlert(pesticide, lat, lng)
                viewModel.updateProfileLocation(lat, lng)
                pesticide = ""
            },
            onError = { isFetching = false; locationError = "Could not get GPS location." }
        )
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {

        // Farmer post section
        if (user?.role == "farmer") item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HoneyCream),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚜", fontSize = 28.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Post Spray Alert", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium, color = BrownInk)
                            Text("Beekeepers within 2km will be notified",
                                style = MaterialTheme.typography.bodySmall, color = BrownMid)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pesticide, onValueChange = { pesticide = it },
                        label = { Text("Pesticide / Chemical Name") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    locationError?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = AlertRed, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            locationError = null
                            when {
                                pesticide.isBlank() -> { locationError = "Enter pesticide name"; return@Button }
                                !locPerm.status.isGranted -> locPerm.launchPermissionRequest()
                                else -> showConfirmDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        enabled = !isFetching
                    ) {
                        if (isFetching) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Getting location...")
                        } else Text("🚨 Alert Beekeepers Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // My alerts acknowledgement tracker (farmer view)
        if (user?.role == "farmer") {
            val myAlerts = alerts.filter { it.farmerId == user?.uid }
            if (myAlerts.isNotEmpty()) item {
                Text("Acknowledgements", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = BrownInk,
                    modifier = Modifier.padding(bottom = 8.dp))
            }
            items(myAlerts) { alert ->
                AckStatusCard(alert)
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        item {
            Text("Active Alerts", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = BrownInk,
                modifier = Modifier.padding(bottom = 8.dp))
        }
        if (alerts.isEmpty()) item {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No active spray alerts", color = BrownMid, fontWeight = FontWeight.SemiBold)
                    Text("All clear for bees! 🐝", color = BrownMid,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        else items(alerts) { alert ->
            SprayAlertCard(
                alert = alert,
                currentUserId = user?.uid ?: "",
                role = user?.role ?: "",
                onDeactivate = { viewModel.deactivateAlert(alert.id) },
                onAcknowledge = { viewModel.acknowledgeAlert(alert.id) }
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("⚠️ Confirm Spray Alert") },
            text = { Text("Your GPS location will be used. All beekeepers within 2km will receive a push notification to close their hives.") },
            confirmButton = {
                Button(onClick = { showConfirmDialog = false; postAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(12.dp)) { Text("Send Alert") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AckStatusCard(alert: SprayAlert) {
    val count = alert.acknowledgedBy.size
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0) ForestLight else HoneyCreamDark)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (count > 0) "✅" else "⏳", fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("${alert.pesticide} — ${formatTimestamp(alert.timestamp)}",
                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (count == 0) "No beekeepers have acknowledged yet"
                    else "$count beekeeper(s) closed their hives 🐝",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (count > 0) SafeGreen else WarnOrange
                )
            }
        }
    }
}

@Composable
fun SprayAlertCard(
    alert: SprayAlert,
    currentUserId: String,
    role: String,
    onDeactivate: () -> Unit,
    onAcknowledge: () -> Unit
) {
    val isAcked = alert.acknowledgedBy.contains(currentUserId)
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlertLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️", fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(alert.farmerName, fontWeight = FontWeight.Bold, color = BrownInk)
                    Text("Pesticide: ${alert.pesticide}",
                        style = MaterialTheme.typography.bodySmall, color = BrownMid)
                    Text(formatTimestamp(alert.timestamp),
                        style = MaterialTheme.typography.bodySmall, color = BrownMid)
                }
            }
            Text("${alert.acknowledgedBy.size} beekeeper(s) closed hives",
                style = MaterialTheme.typography.labelSmall, color = ForestGreen,
                modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (role == "beekeeper") {
                    Button(
                        onClick = { if (!isAcked) onAcknowledge() },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAcked) SafeGreen else HoneyGold)
                    ) {
                        Text(if (isAcked) "✅ Hives Closed" else "Close My Hives",
                            color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (alert.farmerId == currentUserId) {
                    OutlinedButton(
                        onClick = onDeactivate,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Mark Done", style = MaterialTheme.typography.labelLarge) }
                }
            }
        }
    }
}