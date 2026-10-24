package com.xiannn.ledsync.system.data

import android.util.Log
import com.xiannn.ledsync.system.core.AudioGain
import com.xiannn.ledsync.system.core.AudioLedMode
import com.xiannn.ledsync.system.core.DeviceConfig
import com.xiannn.ledsync.system.core.LH8nConfig
import com.xiannn.ledsync.system.core.LedOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class LogLevel { INFO, SUCCESS, WARNING, ERROR }

data class LogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

@Singleton
class HardwareRepository @Inject constructor(
    private val sysfsWriter: SysfsWriter,
    private val preferencesRepository: PreferencesRepository
) {
    private val TAG = "HardwareRepository"
    private var activeConfig: DeviceConfig = LH8nConfig()
    private var isLightActive = false

    var masterEnabled: Boolean = true

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _actionLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val actionLogs: StateFlow<List<LogEntry>> = _actionLogs.asStateFlow()

    private val ownerLock = Any()
    private var currentOwner: LedOwner? = null
    private var savedMusicMode: AudioLedMode? = null
    private var savedMusicGainHex: String? = null

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun getConfig(): DeviceConfig = activeConfig

    fun log(msg: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            message = msg,
            level = level
        )
        val current = _actionLogs.value.toMutableList()
        current.add(entry)
        if (current.size > 200) current.removeAt(0)
        _actionLogs.value = current
        Log.d(TAG, msg)
    }

    fun clearLogs() {
        _actionLogs.value = emptyList()
    }

    fun tryAcquire(owner: LedOwner): Boolean {
        synchronized(ownerLock) {
            val current = currentOwner
            if (current == null || owner.priority >= current.priority) {
                currentOwner = owner
                return true
            }
            log("[${dateFormat.format(Date())}] SUPPRESSED[$owner]: preempted by ${current.name}", LogLevel.WARNING)
            return false
        }
    }

    suspend fun releaseAndRestore(owner: LedOwner, restoreDelayMs: Long = 300L) {
        val (shouldRestore, musicMode, musicGainHex) = synchronized(ownerLock) {
            if (currentOwner == owner) {
                currentOwner = null
            }
            Triple(currentOwner == null, savedMusicMode, savedMusicGainHex)
        }

        if (shouldRestore) {
            val audioEnabled = preferencesRepository.audioLedEnabled.first()
            if (audioEnabled) {
                if (restoreDelayMs > 0) delay(restoreDelayMs)
                val mode = musicMode ?: if (preferencesRepository.audioLedDynamic.first()) AudioLedMode.DYNAMIC else AudioLedMode.STATIC
                val gain = musicGainHex ?: AudioGain.hexForSlider(preferencesRepository.audioLedReactivity.first())
                setAudioReactiveMode(mode, gain)
            }
        }
    }

    fun ensureLedEnabled(force: Boolean = false): Boolean {
        if (isLightActive && !force) return true
        val cfg = activeConfig
        sysfsWriter.writeNode("${cfg.awPath}/hwen", "1")
        sysfsWriter.writeNode("${cfg.awPath}/imax", "c")
        sysfsWriter.writeNode("${cfg.awPath}/brightness", "255")
        sysfsWriter.writeNode("${cfg.awPath}/trigger", "none")
        val ok = sysfsWriter.writeNode(cfg.lbCmd, "00 00 00 00 00 00")
        if (ok) {
            isLightActive = true
            _isReady.value = true
        }
        return ok
    }

    fun initializeHardware(): Boolean {
        log("[${dateFormat.format(Date())}] Initializing hardware components...")
        val ok = ensureLedEnabled(force = true)
        if (ok) {
            log("[${dateFormat.format(Date())}] Hardware initialized via sysfs", LogLevel.SUCCESS)
            log("[${dateFormat.format(Date())}] LED Controller: Active", LogLevel.SUCCESS)
            log("[${dateFormat.format(Date())}] System ready. Awaiting effect selection.", LogLevel.SUCCESS)
            _isReady.value = true
        } else {
            log("[${dateFormat.format(Date())}] Hardware init failed", LogLevel.ERROR)
            _isReady.value = false
        }
        return ok
    }

    suspend fun fireEffect(hex: String, tag: String, owner: LedOwner = LedOwner.MANUAL): Boolean {
        if (!masterEnabled) return false
        if (!tryAcquire(owner)) return false
        ensureLedEnabled()

        val cfg = activeConfig
        sysfsWriter.writeNode(cfg.lbCmd, cfg.turnOffHex)
        sysfsWriter.writeNode(cfg.lbCmd, "00 00 00 00 00 00")
        val ok = sysfsWriter.writeNode(cfg.lbCmd, hex)
        log("[${dateFormat.format(Date())}] FIRE[$tag] -> hex='$hex' ok=$ok", if (ok) LogLevel.SUCCESS else LogLevel.ERROR)
        return ok
    }

    fun fireStop(turnOffHex: String, tag: String, owner: LedOwner = LedOwner.MANUAL) {
        val cfg = activeConfig
        sysfsWriter.writeNode(cfg.lbCmd, turnOffHex)
        log("[${dateFormat.format(Date())}] STOP[$tag] -> hex='$turnOffHex'")
        synchronized(ownerLock) {
            if (currentOwner == owner) {
                currentOwner = null
            }
        }
    }

    fun turnOffAll() {
        val cfg = activeConfig
        sysfsWriter.writeNode(cfg.lbCmd, cfg.turnOffHex)
        synchronized(ownerLock) {
            currentOwner = null
        }
        log("[${dateFormat.format(Date())}] ALL_OFF")
    }

    suspend fun setAudioReactiveMode(mode: AudioLedMode, gainHex: String): Boolean {
        synchronized(ownerLock) {
            savedMusicMode = mode
            savedMusicGainHex = gainHex
        }
        if (!tryAcquire(LedOwner.MUSIC)) return false
        ensureLedEnabled()

        val cfg = activeConfig
        sysfsWriter.writeNode(cfg.lbCmd, cfg.turnOffHex)
        sysfsWriter.writeNode(cfg.lbCmd, "00 00 00 00 00 00")
        val ok = sysfsWriter.writeNode(cfg.lbCmd, mode.hex)
        log("[${dateFormat.format(Date())}] AUDIO_MODE[${mode.label}] -> ok=$ok hex='${mode.hex}'", if (ok) LogLevel.SUCCESS else LogLevel.ERROR)
        if (ok) {
            delay(100L)
            updateVolumeGain(gainHex)
        }
        return ok
    }

    fun updateVolumeGain(gainHex: String): Boolean {
        synchronized(ownerLock) {
            savedMusicGainHex = gainHex
        }
        val cfg = activeConfig
        val cmd = AudioGain.commandForGainHex(gainHex)
        val ok = sysfsWriter.writeNode(cfg.lbCmd, cmd)
        log("[${dateFormat.format(Date())}] GAIN_UPDATE -> '$cmd' ok=$ok")
        return ok
    }

    suspend fun ringPhoneCall(callEffectHex: String): Boolean {
        return fireEffect(callEffectHex, "PHONE_RING", LedOwner.PHONE_CALL)
    }

    suspend fun endPhoneCall() {
        fireStop(activeConfig.turnOffHex, "PHONE_END", LedOwner.PHONE_CALL)
        releaseAndRestore(LedOwner.PHONE_CALL)
    }
}
