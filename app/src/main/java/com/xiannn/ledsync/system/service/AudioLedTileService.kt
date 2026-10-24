package com.xiannn.ledsync.system.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.xiannn.ledsync.system.core.AudioGain
import com.xiannn.ledsync.system.core.AudioLedMode
import com.xiannn.ledsync.system.data.HardwareRepository
import com.xiannn.ledsync.system.data.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioLedTileService : TileService() {

    @Inject lateinit var hardwareRepository: HardwareRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            val enabled = preferencesRepository.audioLedEnabled.first()
            val dynamic = preferencesRepository.audioLedDynamic.first()
            updateTileState(enabled, dynamic)
        }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val current = preferencesRepository.audioLedEnabled.first()
            val next = !current
            preferencesRepository.setAudioLedEnabled(next)

            if (next) {
                val dynamic = preferencesRepository.audioLedDynamic.first()
                val reactivity = preferencesRepository.audioLedReactivity.first()
                val mode = if (dynamic) AudioLedMode.DYNAMIC else AudioLedMode.STATIC
                val gain = AudioGain.hexForSlider(reactivity)
                hardwareRepository.setAudioReactiveMode(mode, gain)
                updateTileState(true, dynamic)
            } else {
                hardwareRepository.turnOffAll()
                updateTileState(false, false)
            }
        }
    }

    private fun updateTileState(enabled: Boolean, dynamic: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = if (enabled) {
            if (dynamic) "Dynamic RGB" else "Static White"
        } else "Off"
        tile.updateTile()
    }
}
