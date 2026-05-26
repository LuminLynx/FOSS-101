package com.perpenda.data.settings

import android.content.Context
import com.perpenda.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user's theme preference (System / Light / Dark) and exposes it
 * as a [StateFlow] so the app root recomposes the moment it changes.
 *
 * Backed by a plain SharedPreferences file — this is non-sensitive UI state, so
 * unlike the encrypted auth prefs it is intentionally left eligible for
 * auto-backup, letting the preference migrate with the user's device
 * (see docs/guides/ANDROID_BEST_PRACTICES.md § 1).
 */
class ThemePreferenceStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(readPersisted())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _mode.value = mode
    }

    private fun readPersisted(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }

    companion object {
        private const val PREFS_NAME = "libella_settings"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
