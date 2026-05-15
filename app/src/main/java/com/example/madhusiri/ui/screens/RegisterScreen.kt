package com.example.madhusiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.madhusiri.ui.navigation.Screen
import com.example.madhusiri.ui.theme.*
import com.example.madhusiri.ui.viewmodel.MainViewModel

@Composable
fun RegisterScreen(navController: NavHostController, viewModel: MainViewModel) {
    var name     by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role     by remember { mutableStateOf("beekeeper") }
    var loading  by remember { mutableStateOf(false) }
    var error    by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.35f)
                .background(Brush.verticalGradient(listOf(ForestGreen, ForestMid)))
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White), contentAlignment = Alignment.Center
            ) { Text("🐝", fontSize = 40.sp) }
            Spacer(Modifier.height(10.dp))
            Text("Join Madhu-Siri", style = MaterialTheme.typography.headlineLarge,
                color = Color.White)
            Text("Register your account", color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp)) {

                    // Role selector
                    Text("I am a:", style = MaterialTheme.typography.titleMedium, color = BrownInk)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("beekeeper" to "🐝 Beekeeper", "farmer" to "🌾 Farmer")
                            .forEach { (value, label) ->
                                val selected = role == value
                                Button(
                                    onClick = { role = value },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) HoneyGold else BrownLight,
                                        contentColor   = if (selected) Color.White else BrownMid
                                    )
                                ) { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                            }
                    }
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = HoneyGold) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = HoneyGold) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password (min 6 chars)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = HoneyGold) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = AlertRed, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            error = null
                            when {
                                name.isBlank()      -> { error = "Enter your name"; return@Button }
                                phone.isBlank()     -> { error = "Enter mobile number"; return@Button }
                                password.length < 6 -> { error = "Password must be 6+ characters"; return@Button }
                            }
                            loading = true
                            viewModel.registerWithPhone(phone.trim(), password, name.trim(), role) { success, msg ->
                                loading = false
                                if (success) navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                } else error = msg
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGold),
                        enabled = !loading
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(22.dp),
                            color = Color.White, strokeWidth = 2.dp)
                        else Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Already registered?", color = BrownMid)
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Login", color = HoneyGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}