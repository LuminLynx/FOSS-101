package com.example.foss101.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foss101.R
import com.example.foss101.data.repository.AuthRepository
import com.example.foss101.data.settings.ThemePreferenceStore
import com.example.foss101.model.User
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.SecondaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.ui.theme.LibellaTheme
import com.example.foss101.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    themePreferenceStore: ThemePreferenceStore,
    onNavigate: (String) -> Unit
) {
    var currentUser by remember { mutableStateOf<User?>(authRepository.currentUser()) }
    val themeMode by themePreferenceStore.mode.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        currentUser = authRepository.currentUser()
        onPauseOrDispose { }
    }

    AppScreenScaffold(
        title = "Settings",
        subtitle = "App preferences and product info"
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .screenContentPadding(contentPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(title = "Account")
            SettingsCard {
                AccountSection(
                    user = currentUser,
                    onSignIn = { onNavigate("auth_login") },
                    onSignUp = { onNavigate("auth_signup") },
                    onSignOut = {
                        authRepository.logout()
                        currentUser = null
                    }
                )
            }

            SectionHeader(title = "Library")
            SettingsCard {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Glossary",
                    description = "Browse the terms reference library",
                    onClick = { onNavigate("glossary") }
                )
            }

            SectionHeader(title = "Appearance")
            SettingsCard {
                ThemeSegmentedControl(
                    selected = themeMode,
                    onSelect = themePreferenceStore::setMode
                )
            }

            SectionHeader(title = "About")
            SettingsCard {
                SettingsRow(
                    icon = ImageVector.vectorResource(R.drawable.ic_libella_mark),
                    title = "Libella",
                    description = "AI-fluent enough to lead the decisions their teams now have to make",
                    onClick = { onNavigate("about") }
                )
            }
        }
    }
}

@Composable
private fun AccountSection(
    user: User?,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onSignOut: () -> Unit
) {
    if (user != null) {
        SettingsRow(
            icon = Icons.Filled.Person,
            title = user.displayName,
            description = user.email
        )
        Divider()
        SettingsRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Sign out",
            description = "End this session on this device",
            onClick = onSignOut
        )
    } else {
        SettingsRow(
            icon = Icons.Filled.PersonOutline,
            title = "Not signed in",
            description = "Sign in to attribute your contributions and earn scores."
        )
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryActionButton(
                text = "Sign in",
                onClick = onSignIn,
                modifier = Modifier.weight(1f)
            )
            SecondaryActionButton(
                text = "Sign up",
                onClick = onSignUp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single-row theme picker: three equal segments (System / Light / Dark)
 * separated by hairline rules. The active segment fills with the oxblood
 * tint (PrimaryContainer — the "focused row" token, Color.kt) and tints its
 * icon + label to match; the others stay muted. Icon-led and compact so the
 * Appearance section keeps the same height as a single row and the screen
 * doesn't need to scroll. No outer border — the enclosing SettingsCard is
 * the frame.
 */
@Composable
private fun ThemeSegmentedControl(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, Icons.Filled.BrightnessAuto, "System"),
        Triple(ThemeMode.LIGHT, Icons.Filled.LightMode, "Light"),
        Triple(ThemeMode.DARK, Icons.Filled.DarkMode, "Dark")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, (mode, icon, label) ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(LibellaTheme.colors.hairline)
                )
            }
            val isSelected = mode == selected
            val tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(mode) }
                    )
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
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
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 14.dp, vertical = 10.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 48.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
