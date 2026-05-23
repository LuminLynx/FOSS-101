package com.example.foss101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foss101.data.repository.RepositoryProvider
import com.example.foss101.navigation.AppNav
import com.example.foss101.ui.theme.Foss101Theme
import com.example.foss101.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RepositoryProvider.init(applicationContext)

        // Required for targeting SDK 35 to handle edge-to-edge correctly
        enableEdgeToEdge()

        setContent {
            val themeStore = remember { RepositoryProvider.themePreferenceStoreInstance }
            val themeMode by themeStore.mode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            Foss101Theme(darkTheme = darkTheme) {
                AppNav()
            }
        }
    }
}
