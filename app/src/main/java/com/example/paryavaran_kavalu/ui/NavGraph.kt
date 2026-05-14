package com.example.paryavaran_kavalu.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.paryavaran_kavalu.ui.screens.DashboardScreen
import com.example.paryavaran_kavalu.ui.screens.MainScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToDashboard = { navController.navigate("dashboard") }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
