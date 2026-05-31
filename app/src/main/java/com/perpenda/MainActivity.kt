package com.perpenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perpenda.data.repository.RepositoryProvider
import com.perpenda.data.version.VersionCheckRepository
import com.perpenda.navigation.AppNav
import com.perpenda.ui.theme.Foss101Theme
import com.perpenda.ui.theme.ThemeMode
import com.perpenda.ui.version.UpdateBanner
import com.perpenda.ui.version.UpdateBannerViewModel

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
                // Update banner sits above the app's nav graph. Renders nothing
                // when the installed build is up to date; ~zero overhead for
                // users on the latest version (the common case).
                val updateBannerViewModel: UpdateBannerViewModel = viewModel {
                    UpdateBannerViewModel(VersionCheckRepository())
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    UpdateBanner(viewModel = updateBannerViewModel)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AppNav()
                    }
                }
            }
        }
    }
}
