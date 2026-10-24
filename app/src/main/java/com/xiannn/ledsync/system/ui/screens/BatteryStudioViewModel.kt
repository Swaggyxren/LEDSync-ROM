package com.xiannn.ledsync.system.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiannn.ledsync.system.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatteryStudioUiState(
    val currentLevel: Int = 76,
    val isCharging: Boolean = false,
    val lowThreshold: Int = 20,
    val critThreshold: Int = 10,
    val fullThreshold: Int = 95,
    val lowEffect: String = "Sunrise",
    val critEffect: String = "Lightning",
    val fullEffect: String = "Pure"
)

@HiltViewModel
class BatteryStudioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _batteryLevel = MutableStateFlow(76)
    private val _isCharging = MutableStateFlow(false)

    val availablePatterns = listOf(
        "Sunrise",
        "Lightning",
        "Pure",
        "Halo",
        "Starburst",
        "Soft",
        "Racing",
        "Dreamy",
        "Breath",
        "Party"
    )

    val uiState: StateFlow<BatteryStudioUiState> = combine(
        _batteryLevel,
        _isCharging,
        preferencesRepository.battLowThreshold,
        preferencesRepository.battCritThreshold,
        preferencesRepository.battFullThreshold,
        preferencesRepository.battLowEffect,
        preferencesRepository.battCritEffect,
        preferencesRepository.battFullEffect
    ) { level, chg, low, crit, full, lowEff, critEff, fullEff ->
        BatteryStudioUiState(
            currentLevel = level,
            isCharging = chg,
            lowThreshold = low,
            critThreshold = crit,
            fullThreshold = full,
            lowEffect = lowEff,
            critEffect = critEff,
            fullEffect = fullEff
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryStudioUiState())

    init {
        pollBattery()
    }

    private fun pollBattery() {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        if (level >= 0 && scale > 0) {
            _batteryLevel.value = (level * 100) / scale
        }
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun updateLowThreshold(threshold: Int) {
        viewModelScope.launch {
            val s = uiState.value
            preferencesRepository.setBatteryThresholds(threshold, s.critThreshold, s.fullThreshold)
        }
    }

    fun updateCritThreshold(threshold: Int) {
        viewModelScope.launch {
            val s = uiState.value
            preferencesRepository.setBatteryThresholds(s.lowThreshold, threshold, s.fullThreshold)
        }
    }

    fun updateFullThreshold(threshold: Int) {
        viewModelScope.launch {
            val s = uiState.value
            preferencesRepository.setBatteryThresholds(s.lowThreshold, s.critThreshold, threshold)
        }
    }

    fun setLowEffect(effect: String) {
        viewModelScope.launch {
            val s = uiState.value
            preferencesRepository.setBatteryEffects(effect, s.critEffect, s.fullEffect)
        }
    }

    fun setCritEffect(effect: String) {
        viewModelScope.launch {
            val s = uiState.value
            preferencesRepository.setBatteryEffects(s.lowEffect, effect, s.fullEffect)
        }
    }

    fun setFullEffect(effect: String) {
        viewModelScope.launch {
            val s = uiState.value
            preferencesRepository.setBatteryEffects(s.lowEffect, s.critEffect, effect)
        }
    }
}
