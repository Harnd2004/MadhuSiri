package com.example.madhusiri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.madhusiri.ui.navigation.MadhuSiriNavHost
import com.example.madhusiri.ui.theme.MadhuSiriTheme
import com.example.madhusiri.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MadhuSiriTheme {
                val navController = rememberNavController()
                val viewModel = viewModel<com.example.madhusiri.ui.viewmodel.MainViewModel>(
                    factory = MainViewModelFactory(applicationContext)
                )
                MadhuSiriNavHost(navController, viewModel)
            }
        }
    }
}