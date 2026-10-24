package com.xiannn.ledsync.system.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiannn.ledsync.system.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TweaksUiState(
    val themeMode: String = "SYSTEM",
    val faceDownMute: Boolean = true,
    val respectDnd: Boolean = true,
    val respectBatterySaver: Boolean = true,
    val cameraSuppression: Boolean = true
)

@HiltViewModel
class TweaksViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<TweaksUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.faceDownMuteEnabled,
        preferencesRepository.respectDnd,
        preferencesRepository.respectBatterySaver,
        preferencesRepository.cameraSuppressionEnabled
    ) { theme, faceDown, dnd, saver, cam ->
        TweaksUiState(
            themeMode = theme,
            faceDownMute = faceDown,
            respectDnd = dnd,
            respectBatterySaver = saver,
            cameraSuppression = cam
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TweaksUiState())

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleFaceDownMute(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setFaceDownMuteEnabled(enabled)
        }
    }

    fun toggleRespectDnd(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRespectDnd(enabled)
        }
    }

    fun toggleRespectBatterySaver(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRespectBatterySaver(enabled)
        }
    }

    fun toggleCameraSuppression(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCameraSuppressionEnabled(enabled)
        }
    }
}
