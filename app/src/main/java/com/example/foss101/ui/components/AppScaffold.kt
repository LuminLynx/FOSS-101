package com.example.foss101.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foss101.ui.theme.LibellaTheme

// Flat editorial app bar (la-bar): paper background, no shadow, a single
// bottom hairline. Serif title, uppercase UI eyebrow subtitle.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreenScaffold(
    title: String,
    subtitle: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle.uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = LibellaTheme.colors.hairline)
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

fun Modifier.screenContentPadding(contentPadding: PaddingValues): Modifier =
    this
        .fillMaxSize()
        .padding(contentPadding)
        .padding(horizontal = 20.dp, vertical = 12.dp)
