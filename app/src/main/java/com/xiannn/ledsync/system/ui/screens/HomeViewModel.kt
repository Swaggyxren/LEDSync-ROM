package com.xiannn.ledsync.system.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiannn.ledsync.system.data.HardwareRepository
import com.xiannn.ledsync.system.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class HomeUiState(
    val masterEnabled: Boolean = true,
    val isSysfsReady: Boolean = false,
    val batteryPct: Int = 100,
    val isCharging: Boolean = false,
    val audioLedActive: Boolean = false,
    val cpuUsagePct: Int = 12,
    val ramUsedGb: Float = 3.2f,
    val ramTotalGb: Float = 8.0f,
    val deviceModel: String = "TECNO POVA 5 Pro (LH8n)",
    val androidVersion: String = "Android 14 (LineageOS)",
    val kernelRelease: String = System.getProperty("os.version") ?: "5.10.x-android"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hardwareRepository: HardwareRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _batteryPct = MutableStateFlow(80)
    private val _isCharging = MutableStateFlow(false)
    private val _cpuUsage = MutableStateFlow(15)
    private val _ramUsed = MutableStateFlow(3.4f)
    private val _ramTotal = MutableStateFlow(8.0f)

    val uiState: StateFlow<HomeUiState> = combine(
        preferencesRepository.masterEnabled,
        hardwareRepository.isReady,
        preferencesRepository.audioLedEnabled,
        _batteryPct,
        _isCharging
    ) { master, ready, audioActive, batt, chg ->
        updateMemoryInfo()
        HomeUiState(
            masterEnabled = master,
            isSysfsReady = ready,
            batteryPct = batt,
            isCharging = chg,
            audioLedActive = audioActive,
            cpuUsagePct = _cpuUsage.value,
            ramUsedGb = _ramUsed.value,
            ramTotalGb = _ramTotal.value,
            deviceModel = if (Build.MODEL.contains(Build.DEVICE, ignoreCase = true)) {
                "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}"
            } else {
                "${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (${Build.DEVICE})"
            },
            androidVersion = "Android ${Build.VERSION.RELEASE} (${Build.ID})",
            kernelRelease = System.getProperty("os.version") ?: "5.10-kernel"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        pollBattery()
        updateMemoryInfo()
    }

    private fun pollBattery() {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        if (level >= 0 && scale > 0) {
            _batteryPct.value = (level * 100) / scale
        }
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun updateMemoryInfo() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val total = memInfo.totalMem / (1024f * 1024f * 1024f)
            val free = memInfo.availMem / (1024f * 1024f * 1024f)
            val used = total - free
            _ramTotal.value = String.format("%.1f", total).toFloat()
            _ramUsed.value = String.format("%.1f", used).toFloat()
        } catch (_: Exception) {}
    }

    fun toggleMasterPower() {
        viewModelScope.launch {
            val current = uiState.value.masterEnabled
            val next = !current
            preferencesRepository.setMasterEnabled(next)
            hardwareRepository.masterEnabled = next
            if (!next) {
                hardwareRepository.turnOffAll()
            } else {
                hardwareRepository.initializeHardware()
            }
        }
    }
}
