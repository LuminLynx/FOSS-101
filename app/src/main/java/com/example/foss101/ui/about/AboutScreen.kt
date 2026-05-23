package com.example.foss101.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.foss101.BuildConfig
import com.example.foss101.R
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.ui.theme.LibellaTheme

private const val FEEDBACK_EMAIL = "libella@pm.me"
private const val TAGLINE =
    "AI-fluent enough to lead the decisions their teams now have to make"
private const val PRODUCT_DESCRIPTION =
    "The canonical curriculum: the LLM concepts a product manager actually has to " +
        "reason about — from tokenization and context windows to evals, RAG, and the " +
        "production trade-offs beyond — taught one trade-off at a time."

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    AppScreenScaffold(title = "About", subtitle = "Libella") { contentPadding ->
        Column(
            modifier = Modifier
                .screenContentPadding(contentPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_libella_mark),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(text = "Libella", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(text = TAGLINE, style = MaterialTheme.typography.titleMedium)

            Text(
                text = PRODUCT_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionHeader(title = "Feedback")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, LibellaTheme.colors.hairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendFeedback(context) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MailOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Send feedback",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = FEEDBACK_EMAIL,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun sendFeedback(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$FEEDBACK_EMAIL")
        putExtra(Intent.EXTRA_SUBJECT, "Libella feedback")
        putExtra(Intent.EXTRA_TEXT, "\n\n—\nLibella v${BuildConfig.VERSION_NAME}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    // ACTION_SENDTO + mailto resolves only to email apps; fail quietly if the
    // device has none rather than crash on ActivityNotFoundException.
    runCatching { context.startActivity(intent) }
}
