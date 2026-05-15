package com.example.madhusiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.madhusiri.ui.navigation.Screen
import com.example.madhusiri.ui.theme.*
import com.example.madhusiri.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(navController: NavHostController, viewModel: MainViewModel) {
    var phone    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading  by remember { mutableStateOf(false) }
    var error    by remember { mutableStateOf<String?>(null) }
    val context  = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    Brush.verticalGradient(listOf(HoneyGold, HoneyLight))
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("🍯", fontSize = 44.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("Madhu-Siri", style = MaterialTheme.typography.displayLarge,
                color = Color.White, fontWeight = FontWeight.Bold)
            Text("Bee-Farmer Harmony", color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(36.dp))

            // Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Welcome Back", style = MaterialTheme.typography.headlineMedium,
                        color = BrownInk)
                    Text("Login with your mobile number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = HoneyGold) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = HoneyGold) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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
                            if (phone.isBlank() || password.isBlank()) {
                                error = "Please fill in all fields"; return@Button
                            }
                            loading = true
                            viewModel.signInWithPhone(phone.trim(), password) { success, msg ->
                                loading = false
                                if (success) {
                                    viewModel.saveLocationToProfile(context)
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
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
                        else Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("New here?", color = BrownMid)
                        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                            Text("Register", color = HoneyGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}