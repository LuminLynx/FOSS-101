package com.example.foss101.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foss101.ui.ai.AiToolsScreen
import com.example.foss101.ui.browse.BrowseTermsScreen
import com.example.foss101.ui.chat.ChatScreen
import com.example.foss101.ui.details.TermDetailsScreen
import com.example.foss101.ui.home.HomeScreen
import com.example.foss101.ui.settings.SettingsScreen
import com.example.foss101.ui.trendwatcher.TrendWatcherScreen

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigate = navController::navigate)
        }
        composable("browse") { BrowseTermsScreen() }
        composable("details") { TermDetailsScreen() }
        composable("ai_tools") { AiToolsScreen() }
        composable("trend_watcher") { TrendWatcherScreen() }
        composable("chat") { ChatScreen() }
        composable("settings") { SettingsScreen() }
    }
}
