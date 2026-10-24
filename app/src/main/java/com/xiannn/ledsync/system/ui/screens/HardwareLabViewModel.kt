package com.xiannn.ledsync.system.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiannn.ledsync.system.core.LedOwner
import com.xiannn.ledsync.system.data.HardwareRepository
import com.xiannn.ledsync.system.data.LogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HardwareLabViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository
) : ViewModel() {

    val actionLogs: StateFlow<List<LogEntry>> = hardwareRepository.actionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val effectList: List<String> = listOf(
        "Soft",
        "Racing",
        "Dreamy",
        "Breath",
        "Party",
        "Pure",
        "Starburst",
        "Halo",
        "Lightning",
        "Sunrise",
        "Low Battery",
        "Charging Tier 1 (0-20%)",
        "Charging Tier 2 (20-60%)",
        "Charging Tier 3 (60-100%)",
        "Full Charge"
    )

    fun testEffect(name: String) {
        viewModelScope.launch {
            val hex = hardwareRepository.getConfig().ledEffects[name] ?: return@launch
            hardwareRepository.fireEffect(hex, name, LedOwner.MANUAL)
        }
    }

    fun turnOff() {
        hardwareRepository.turnOffAll()
    }

    fun clearConsole() {
        hardwareRepository.clearLogs()
    }
}
