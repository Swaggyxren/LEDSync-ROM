package com.xiannn.ledsync.system.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiannn.ledsync.system.core.AudioGain
import com.xiannn.ledsync.system.core.AudioLedMode
import com.xiannn.ledsync.system.data.HardwareRepository
import com.xiannn.ledsync.system.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioReactiveUiState(
    val isEnabled: Boolean = false,
    val isDynamicRgb: Boolean = true,
    val reactivity: Int = 60,
    val dynamicVolumeTracking: Boolean = true
)

@HiltViewModel
class AudioReactiveViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<AudioReactiveUiState> = combine(
        preferencesRepository.audioLedEnabled,
        preferencesRepository.audioLedDynamic,
        preferencesRepository.audioLedReactivity,
        preferencesRepository.dynamicVolumeTracking
    ) { enabled, dynamic, reactivity, volTracking ->
        AudioReactiveUiState(
            isEnabled = enabled,
            isDynamicRgb = dynamic,
            reactivity = reactivity,
            dynamicVolumeTracking = volTracking
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioReactiveUiState())

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAudioLedEnabled(enabled)
            if (enabled) {
                val s = uiState.value
                val mode = if (s.isDynamicRgb) AudioLedMode.DYNAMIC else AudioLedMode.STATIC
                val gain = AudioGain.hexForSlider(s.reactivity)
                hardwareRepository.setAudioReactiveMode(mode, gain)
            } else {
                hardwareRepository.turnOffAll()
            }
        }
    }

    fun toggleDynamicRgb(isDynamic: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAudioLedDynamic(isDynamic)
            if (uiState.value.isEnabled) {
                val mode = if (isDynamic) AudioLedMode.DYNAMIC else AudioLedMode.STATIC
                val gain = AudioGain.hexForSlider(uiState.value.reactivity)
                hardwareRepository.setAudioReactiveMode(mode, gain)
            }
        }
    }

    fun updateReactivity(reactivity: Int) {
        viewModelScope.launch {
            preferencesRepository.setAudioLedReactivity(reactivity)
            if (uiState.value.isEnabled) {
                val gain = AudioGain.hexForSlider(reactivity)
                hardwareRepository.updateVolumeGain(gain)
            }
        }
    }

    fun toggleDynamicVolumeTracking(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicVolumeTracking(enabled)
        }
    }
}
