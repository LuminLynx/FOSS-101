package com.perpenda.ui.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perpenda.BuildConfig
import com.perpenda.data.version.VersionCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UpdateBannerState {
    data object Hidden : UpdateBannerState
    data class Available(val versionName: String, val downloadUrl: String) : UpdateBannerState
}

class UpdateBannerViewModel(
    private val repository: VersionCheckRepository,
    private val currentVersionCode: Int = BuildConfig.VERSION_CODE,
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateBannerState>(UpdateBannerState.Hidden)
    val state: StateFlow<UpdateBannerState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val latest = repository.fetchLatest() ?: return@launch
            if (latest.versionCode > currentVersionCode) {
                _state.value = UpdateBannerState.Available(
                    versionName = latest.versionName,
                    downloadUrl = latest.downloadUrl,
                )
            }
        }
    }

    fun dismiss() {
        _state.value = UpdateBannerState.Hidden
    }
}
