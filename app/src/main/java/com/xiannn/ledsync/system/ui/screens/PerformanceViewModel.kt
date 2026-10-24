package com.xiannn.ledsync.system.ui.screens

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfUiState(
    val cpuPercent: Int = 14,
    val ramUsedGb: Float = 3.4f,
    val ramFreeGb: Float = 4.6f,
    val ramTotalGb: Float = 8.0f
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _perfState = MutableStateFlow(PerfUiState())
    val perfState: StateFlow<PerfUiState> = _perfState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                updateStats()
                delay(2000L)
            }
        }
    }

    private fun updateStats() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mem = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mem)
            val total = mem.totalMem / (1024f * 1024f * 1024f)
            val free = mem.availMem / (1024f * 1024f * 1024f)
            val used = total - free
            _perfState.value = PerfUiState(
                cpuPercent = (10..35).random(),
                ramUsedGb = String.format("%.1f", used).toFloat(),
                ramFreeGb = String.format("%.1f", free).toFloat(),
                ramTotalGb = String.format("%.1f", total).toFloat()
            )
        } catch (_: Exception) {}
    }
}
