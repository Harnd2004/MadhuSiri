package com.example.madhusiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp  // ← CHANGED
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.madhusiri.ui.navigation.Screen
import com.example.madhusiri.ui.theme.*
import com.example.madhusiri.ui.viewmodel.MainViewModel
import com.example.madhusiri.utils.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: MainViewModel) {
    val user    by viewModel.userProfile.collectAsState()
    val alerts  by viewModel.sprayAlerts.collectAsState()
    val hives   by viewModel.hives.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.saveLocationToProfile(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) { Text("🍯", fontSize = 18.sp) }
                        Spacer(Modifier.width(10.dp))
                        Text("Madhu-Siri", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HoneyGold),
                actions = {
                    IconButton(onClick = {
                        viewModel.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White)  // ← CHANGED
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {

            // Hero banner
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(HoneyGold, HoneyLight)))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text("Hello, ${user?.name?.split(" ")?.first() ?: "Friend"}! 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (user?.role == "beekeeper") "Protecting your hives 🐝"
                            else "Farming sustainably 🌾",
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatChip("${hives.filter { it.ownerId == user?.uid }.size}", "My Hives", "🐝")
                            StatChip("${alerts.size}", "Alerts", "🚨")
                        }
                    }
                }
            }

            // Active spray alert banner
            if (alerts.isNotEmpty()) item {
                Spacer(Modifier.height(12.dp))
                alerts.take(1).forEach { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AlertLight)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 28.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("SPRAY IN PROGRESS", fontWeight = FontWeight.Bold,
                                    color = AlertRed, style = MaterialTheme.typography.labelLarge)
                                Text("${alert.farmerName} is spraying ${alert.pesticide}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text(formatTimestamp(alert.timestamp),
                                    style = MaterialTheme.typography.bodySmall, color = BrownMid)
                            }
                            if (user?.role == "beekeeper") {
                                val acked = alert.acknowledgedBy.contains(user?.uid)
                                Button(
                                    onClick = { if (!acked) viewModel.acknowledgeAlert(alert.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (acked) SafeGreen else AlertRed
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (acked) "✅ Closed" else "Close Hives",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Acknowledgement status for farmers
            if (user?.role == "farmer" && alerts.isNotEmpty()) item {
                Spacer(Modifier.height(8.dp))
                alerts.filter { it.farmerId == user?.uid }.forEach { alert ->
                    val count = alert.acknowledgedBy.size
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ForestLight)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🐝", fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Hive Closures for your alert",
                                    fontWeight = FontWeight.SemiBold, color = ForestGreen)
                                Text("$count beekeeper(s) have closed their hives",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (count > 0) SafeGreen else BrownMid)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = BrownInk)
            }
            item { QuickActionsGrid(navController, user?.role ?: "") }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatChip(value: String, label: String, emoji: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 18.sp)
            Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
            Text(label, color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun QuickActionsGrid(navController: NavHostController, role: String) {
    val items = listOf(
        Triple("🗺️", "Hive Map",      Screen.HiveMap.route),
        Triple("🚨", "Spray Alert",    Screen.SprayAlert.route),
        Triple("❤️", "Health Check",   Screen.HealthCheck.route),
        Triple("🌿", "Bee Tips",       Screen.Tips.route)
    )
    Column(Modifier.padding(horizontal = 16.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)) {
                row.forEach { (emoji, title, route) ->
                    Card(
                        onClick = { navController.navigate(route) },
                        modifier = Modifier.weight(1f).height(110.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = HoneyCream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 30.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(title, fontWeight = FontWeight.SemiBold, color = BrownInk,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}