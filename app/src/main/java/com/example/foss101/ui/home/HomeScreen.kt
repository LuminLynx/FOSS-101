package com.example.foss101.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val HomeRoutes = listOf(
    "browse" to "Browse Terms",
    "categories" to "Categories",
    "search" to "Search",
    "ai_tools" to "AI Tools",
    "trend_watcher" to "Trend Watcher",
    "ask_glossary" to "Ask the Glossary",
    "settings" to "Settings"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("FOSS Glossary") })
        }
    ) { innerPadding ->
        HomeScreenContent(
            routes = HomeRoutes,
            onNavigate = onNavigate,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun HomeScreenContent(
    routes: List<Pair<String, String>>,
    onNavigate: (String) -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        routes.forEach { (route, label) ->
            Button(
                onClick = { onNavigate(route) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 20.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(text = label)
            }
        }
    }
}
