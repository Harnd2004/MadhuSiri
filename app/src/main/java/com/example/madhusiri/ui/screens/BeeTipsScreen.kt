package com.example.madhusiri.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.madhusiri.ui.theme.*

data class Tip(val emoji: String, val name: String, val desc: String, val safety: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeeTipsScreen(navController: NavHostController) {
    val tips = listOf(
        Tip("🌿", "Neem Oil",           "Organic and bee-safe when applied in evenings. Avoid direct flower spray.", "SAFE"),
        Tip("🌸", "Pyrethrin",          "Low bee toxicity when dry. Apply only after dusk when bees are back in hives.", "MODERATE"),
        Tip("🌾", "Kaolin Clay",        "Physical barrier pest control. Completely bee-safe, no chemicals.", "SAFE"),
        Tip("🐞", "Bt (Bacillus t.)",   "Biological control targeting caterpillars only. Fully bee-safe.", "SAFE"),
        Tip("⚠️", "Neonicotinoids",     "Highly toxic to bees. Avoid during flowering season entirely.", "DANGEROUS"),
        Tip("⛔", "Organophosphates",   "Extremely toxic. Never use near hives or blooming crops.", "DANGEROUS"),
        Tip("🕐", "Spray at Dusk",      "Always spray between 6–8 PM when bees return to hives.", "PRACTICE"),
        Tip("📞", "Alert Beekeepers",   "Notify beekeepers 24 hours before any planned spraying activity.", "PRACTICE"),
        Tip("🌺", "Avoid Bloom Time",   "Never spray when crops are in full flower — peak bee foraging hours.", "PRACTICE"),
        Tip("💧", "Dilute Properly",    "Always follow label dilution. Concentrated sprays near hives are fatal.", "PRACTICE")
    )

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("🌿 Bee-Friendly Tips") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestGreen,
                titleContentColor = Color.White, navigationIconContentColor = Color.White)
        )
    }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            item {
                Text("Safe Pesticide Guide", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text("Protect your crop AND the pollinators 🐝",
                    style = MaterialTheme.typography.bodyMedium, color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp))
            }
            items(tips) { tip ->
                val (bg, fg) = when (tip.safety) {
                    "SAFE"      -> Color(0xFFE8F5E9) to SafeGreen
                    "MODERATE"  -> Color(0xFFFFF8E1) to WarnOrange
                    "DANGEROUS" -> Color(0xFFFFEBEE) to AlertRed
                    else        -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp)) {
                        Text(tip.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(tip.name, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.width(8.dp))
                                Surface(color = fg.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                    Text(tip.safety, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = fg, style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(tip.desc, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}