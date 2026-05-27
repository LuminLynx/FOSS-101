package com.perpenda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perpenda.ui.theme.PerpendaTheme

// Flat hairline status cards — paper surface, single rule, no elevation.

@Composable
private fun StateCard(modifier: Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, PerpendaTheme.colors.hairline),
        content = { content() }
    )
}

@Composable
fun LoadingState(message: String, modifier: Modifier = Modifier) {
    StateCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    StateMessage(message, modifier, MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier) {
    StateMessage(message, modifier, MaterialTheme.colorScheme.error)
}

@Composable
private fun StateMessage(
    message: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    StateCard(modifier) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        )
    }
}
