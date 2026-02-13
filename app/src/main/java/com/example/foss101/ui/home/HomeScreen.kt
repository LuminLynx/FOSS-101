package com.example.foss101.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val HomeRoutes = listOf(
    "browse" to "Browse Terms",
    "details" to "Term Details",
    "ai_tools" to "AI Tools",
    "trend_watcher" to "Trend Watcher",
    "chat" to "Chat",
    "settings" to "Settings"
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FOSS 101",
            style = MaterialTheme.typography.headlineLarge
        )

        HomeRoutes.forEach { (route, label) ->
            Button(
                onClick = { onNavigate(route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
