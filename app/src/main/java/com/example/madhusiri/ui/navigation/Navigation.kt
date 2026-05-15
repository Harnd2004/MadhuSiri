package com.example.madhusiri.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.madhusiri.ui.screens.*
import com.example.madhusiri.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Login       : Screen("login")
    object Register    : Screen("register")
    object Home        : Screen("home")
    object HiveMap     : Screen("hive_map")
    object SprayAlert  : Screen("spray_alert")
    object HealthCheck : Screen("health_check")
    object Tips        : Screen("tips")
}

@Composable
fun MadhuSiriNavHost(navController: NavHostController, viewModel: MainViewModel) {
    val start = if (viewModel.isLoggedIn) Screen.Home.route else Screen.Login.route
    NavHost(navController, startDestination = start) {
        composable(Screen.Login.route)       { LoginScreen(navController, viewModel) }
        composable(Screen.Register.route)    { RegisterScreen(navController, viewModel) }
        composable(Screen.Home.route)        { HomeScreen(navController, viewModel) }
        composable(Screen.HiveMap.route)     { HiveMapScreen(navController, viewModel) }
        composable(Screen.SprayAlert.route)  { SprayAlertScreen(navController, viewModel) }
        composable(Screen.HealthCheck.route) { HealthCheckScreen(navController, viewModel) }
        composable(Screen.Tips.route)        { BeeTipsScreen(navController) }
    }
}