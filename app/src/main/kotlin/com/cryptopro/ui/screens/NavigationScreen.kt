package com.cryptopro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Trading : Screen("trading")
    object Backtest : Screen("backtest")
    object Portfolio : Screen("portfolio")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(androidx.compose.material.icons.filled.Home, null) },
                    label = { Text("Trading") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Trading.route) }
                )
                NavigationBarItem(
                    icon = { Icon(androidx.compose.material.icons.filled.Assessment, null) },
                    label = { Text("Backtest") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Backtest.route) }
                )
                NavigationBarItem(
                    icon = { Icon(androidx.compose.material.icons.filled.AccountBalance, null) },
                    label = { Text("Portfolio") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Portfolio.route) }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Trading.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Trading.route) {
                TradingScreen()
            }
            composable(Screen.Backtest.route) {
                BacktestScreen()
            }
            composable(Screen.Portfolio.route) {
                PortfolioScreen()
            }
        }
    }
}
