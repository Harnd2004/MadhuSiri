package com.example.madhusiri.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.madhusiri.data.models.Hive
import com.example.madhusiri.data.models.HiveHealthLog
import com.example.madhusiri.data.repository.FirestoreRepository
import com.example.madhusiri.ui.theme.*
import com.example.madhusiri.ui.viewmodel.MainViewModel
import com.example.madhusiri.utils.formatTimestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(navController: NavHostController, viewModel: MainViewModel) {
    val user  by viewModel.userProfile.collectAsState()
    val hives by viewModel.hives.collectAsState()
    val myHives = hives.filter { it.ownerId == user?.uid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("❤️ Hive Health") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HoneyGold,    // ← was HoneyAmber
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            if (myHives.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🐝", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No hives yet", fontWeight = FontWeight.SemiBold, color = BrownInk)
                            Text("Add hives on the map first!",
                                style = MaterialTheme.typography.bodySmall, color = BrownMid)
                        }
                    }
                }
            } else {
                items(myHives) { hive ->
                    HiveHealthCard(hive, viewModel)
                }
            }
        }
    }
}

@Composable
fun HiveHealthCard(hive: Hive, viewModel: MainViewModel) {
    var expanded      by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    val scope         = rememberCoroutineScope()

    val (healthColor, healthBg) = when (hive.healthStatus) {
        "good" -> SafeGreen  to Color(0xFFE8F5E9)
        "fair" -> WarnOrange to Color(0xFFFFF8E1)
        else   -> AlertRed   to AlertLight
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🐝", fontSize = 26.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(hive.name, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium, color = BrownInk)
                    Text("${hive.honeyProduction}kg honey produced",
                        style = MaterialTheme.typography.bodySmall, color = BrownMid)
                }
                Surface(
                    color = healthBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        hive.healthStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = healthColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(10.dp))
                Text("Last checked: ${formatTimestamp(hive.lastChecked)}",
                    style = MaterialTheme.typography.bodySmall, color = BrownMid)
                Text("Notes: ${hive.notes.ifEmpty { "None" }}",
                    style = MaterialTheme.typography.bodySmall, color = BrownMid)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showLogDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("📋 Log Update") }
                    Button(
                        onClick = { viewModel.deleteHive(hive.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) { Text("🗑 Delete") }
                }
            }
        }
    }

    if (showLogDialog) {
        HiveHealthLogDialog(
            onDismiss = { showLogDialog = false },
            onLog = { status, honey, notes ->
                val log = HiveHealthLog(
                    hiveId        = hive.id,
                    healthStatus  = status,
                    honeyProduction = honey,
                    notes         = notes
                )
                val updated = hive.copy(
                    healthStatus    = status,
                    honeyProduction = honey,
                    notes           = notes,
                    lastChecked     = System.currentTimeMillis()
                )
                viewModel.addHealthLog(log, updated)
                showLogDialog = false
            }
        )
    }
}

@Composable
fun HiveHealthLogDialog(
    onDismiss: () -> Unit,
    onLog: (String, Double, String) -> Unit
) {
    var status  by remember { mutableStateOf("good") }
    var honey   by remember { mutableStateOf("") }
    var notes   by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("📋 Log Health Update") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Health Status:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("good", "fair", "poor").forEach { s ->
                        val selected = status == s
                        val color = when (s) {
                            "good" -> SafeGreen; "fair" -> WarnOrange; else -> AlertRed
                        }
                        FilterChip(
                            selected = selected,
                            onClick  = { status = s },
                            label    = { Text(s.replaceFirstChar { it.uppercase() }) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor     = color
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = honey, onValueChange = { honey = it },
                    label = { Text("Honey Produced (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLog(status, honey.toDoubleOrNull() ?: 0.0, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = HoneyGold),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Save Log") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}