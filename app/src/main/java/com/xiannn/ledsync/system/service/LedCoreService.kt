package com.xiannn.ledsync.system.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.xiannn.ledsync.system.core.AudioGain
import com.xiannn.ledsync.system.core.LedOwner
import com.xiannn.ledsync.system.data.HardwareRepository
import com.xiannn.ledsync.system.data.PreferencesRepository
import com.xiannn.ledsync.system.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LedCoreService : NotificationListenerService() {

    private val TAG = "LedCoreService"

    @Inject lateinit var hardwareRepository: HardwareRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var powerManager: PowerManager
    private lateinit var notifManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private var sensorManager: SensorManager? = null
    private var cameraManager: CameraManager? = null

    private val CHANNEL_ID = "ledsync_rom_service"
    private val FG_NOTIF_ID = 2001

    private val SEQUENCE_COOLDOWN_MS = 6000L
    private val DOUBLE_FIRE_DELAY_MS = 1500L
    private val LOOP_AUTO_STOP_MS = 5000L

    private val lastTriggerPerPkg = HashMap<String, Long>()
    private val activeLoopingPkgs = mutableSetOf<String>()

    private var batteryReceiverRegistered = false
    private var volumeReceiverRegistered = false

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null

    // Face-down sensor state (Accelerometer Gz)
    private var isFaceDown = false
    private var gzPrevious = 0.0f
    private var gzInvertedCount = 0
    private val MAX_COUNT_GZ_CHANGE = 10

    // Rear camera state
    private var isRearCameraInUse = false

    // Battery state tracking
    private var lastChargingState: Boolean? = null
    private var hasFlashedFullThisSession = false
    private var chargeTierJob: Job? = null
    private var lowBatteryJob: Job? = null

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val gz = event.values[2]
                if (gzPrevious == 0.0f) {
                    gzPrevious = gz
                    return
                }
                if (gzPrevious * gz < 0.0f) {
                    gzInvertedCount++
                    if (gzInvertedCount >= MAX_COUNT_GZ_CHANGE) {
                        gzPrevious = gz
                        gzInvertedCount = 0
                        if (gz < 0.0f) {
                            // Phone placed face-down on flat surface
                            isFaceDown = true
                            serviceScope.launch {
                                if (preferencesRepository.faceDownMuteEnabled.first()) {
                                    hardwareRepository.turnOffAll()
                                }
                            }
                        } else {
                            // Phone flipped face-up
                            isFaceDown = false
                        }
                    }
                } else if (gzInvertedCount > 0) {
                    gzPrevious = gz
                    gzInvertedCount = 0
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val cameraCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            super.onCameraUnavailable(cameraId)
            if (isRearCamera(cameraId)) {
                isRearCameraInUse = true
                serviceScope.launch {
                    if (preferencesRepository.cameraSuppressionEnabled.first()) {
                        hardwareRepository.turnOffAll()
                    }
                }
            }
        }

        override fun onCameraAvailable(cameraId: String) {
            super.onCameraAvailable(cameraId)
            if (isRearCamera(cameraId)) {
                isRearCameraInUse = false
                serviceScope.launch {
                    hardwareRepository.releaseAndRestore(LedOwner.MANUAL)
                }
            }
        }
    }

    private fun isRearCamera(cameraId: String): Boolean {
        return try {
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_BACK
        } catch (_: Exception) {
            false
        }
    }

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if ("android.media.VOLUME_CHANGED_ACTION" == intent.action) {
                val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                if (streamType == AudioManager.STREAM_MUSIC) {
                    serviceScope.launch {
                        val dynamicTracking = preferencesRepository.dynamicVolumeTracking.first()
                        val audioEnabled = preferencesRepository.audioLedEnabled.first()
                        if (dynamicTracking && audioEnabled) {
                            val vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val gainHex = AudioGain.hexForVolume(vol)
                            handler.removeCallbacksAndMessages("VOL_DEBOUNCE")
                            handler.postDelayed({
                                hardwareRepository.updateVolumeGain(gainHex)
                            }, 100L)
                        }
                    }
                }
            }
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val pct = if (level >= 0 && scale > 0) (level * 100) / scale else -1
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                if (pct >= 0) {
                    serviceScope.launch {
                        handleBatteryChanged(pct, charging)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(PowerManager::class.java)
        notifManager = getSystemService(NotificationManager::class.java)
        audioManager = getSystemService(AudioManager::class.java)
        sensorManager = getSystemService(SensorManager::class.java)
        cameraManager = getSystemService(CameraManager::class.java)
        createNotificationChannel()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(FG_NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(FG_NOTIF_ID, buildNotification())
            }
        } catch (e: Exception) {
            Log.e("LedCoreService", "Failed to startForeground in onCreate", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(FG_NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(FG_NOTIF_ID, buildNotification())
            }
        } catch (e: Exception) {
            Log.e("LedCoreService", "Failed to startForeground in onStartCommand", e)
        }
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        startForeground(FG_NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        hardwareRepository.initializeHardware()

        if (!batteryReceiverRegistered) {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryReceiverRegistered = true
        }

        if (!volumeReceiverRegistered) {
            registerReceiver(volumeReceiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
            volumeReceiverRegistered = true
        }

        registerSensor()
        registerCameraListener()
        registerPhoneCallListener()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        unregisterReceivers()
        unregisterSensor()
        unregisterCameraListener()
        unregisterPhoneCallListener()
        requestRebind(ComponentName(this, LedCoreService::class.java))
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        handler.removeCallbacksAndMessages(null)
        serviceScope.cancel()
        unregisterReceivers()
        unregisterSensor()
        unregisterCameraListener()
        unregisterPhoneCallListener()
        super.onDestroy()
    }

    private fun registerSensor() {
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            sensorManager?.registerListener(sensorListener, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterSensor() {
        sensorManager?.unregisterListener(sensorListener)
    }

    private fun registerCameraListener() {
        try {
            cameraManager?.registerAvailabilityCallback(cameraCallback, handler)
        } catch (e: Exception) {
            Log.w(TAG, "Cannot register camera callback: ${e.message}")
        }
    }

    private fun unregisterCameraListener() {
        try {
            cameraManager?.unregisterAvailabilityCallback(cameraCallback)
        } catch (_: Exception) {}
    }

    private fun unregisterReceivers() {
        if (batteryReceiverRegistered) {
            try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
            batteryReceiverRegistered = false
        }
        if (volumeReceiverRegistered) {
            try { unregisterReceiver(volumeReceiver) } catch (_: Exception) {}
            volumeReceiverRegistered = false
        }
    }

    @Suppress("DEPRECATION")
    private fun registerPhoneCallListener() {
        if (phoneStateListener != null) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val tm = getSystemService(TelephonyManager::class.java) ?: return
        val listener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                serviceScope.launch {
                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING -> {
                            if (shouldSuppressLed()) return@launch
                            val effectName = preferencesRepository.callEffect.first()
                            val hex = hardwareRepository.getConfig().ledEffects[effectName]
                                ?: "00 04 00 00 00 00"
                            hardwareRepository.ringPhoneCall(hex)
                        }
                        TelephonyManager.CALL_STATE_OFFHOOK,
                        TelephonyManager.CALL_STATE_IDLE -> {
                            hardwareRepository.endPhoneCall()
                        }
                    }
                }
            }
        }
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        telephonyManager = tm
        phoneStateListener = listener
    }

    @Suppress("DEPRECATION")
    private fun unregisterPhoneCallListener() {
        phoneStateListener?.let { telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE) }
        phoneStateListener = null
        telephonyManager = null
    }

    private suspend fun shouldSuppressLed(): Boolean {
        if (!hardwareRepository.masterEnabled) return true

        // 1. Face-down mute
        if (isFaceDown && preferencesRepository.faceDownMuteEnabled.first()) {
            return true
        }

        // 2. Rear camera active
        if (isRearCameraInUse && preferencesRepository.cameraSuppressionEnabled.first()) {
            return true
        }

        // 3. Do Not Disturb (Zen Mode)
        if (preferencesRepository.respectDnd.first()) {
            val filter = notifManager.currentInterruptionFilter
            if (filter != NotificationManager.INTERRUPTION_FILTER_ALL && filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN) {
                return true
            }
        }

        // 4. Battery Saver (Low Power Mode)
        if (preferencesRepository.respectBatterySaver.first()) {
            if (powerManager.isPowerSaveMode) {
                return true
            }
        }

        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        serviceScope.launch {
            try {
                if (shouldSuppressLed()) return@launch

                val pkg = sbn.packageName ?: return@launch
                val hexMap = preferencesRepository.notifHexMap.first()
                val hex = hexMap[pkg] ?: return@launch
                if (hex.isBlank()) return@launch

                val now = System.currentTimeMillis()
                val last = synchronized(lastTriggerPerPkg) { lastTriggerPerPkg[pkg] ?: 0L }
                if (now - last < SEQUENCE_COOLDOWN_MS) return@launch

                triggerNotifEffect(pkg, hex)
            } catch (t: Throwable) {
                Log.e(TAG, "onNotificationPosted failed: ${t.message}", t)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        serviceScope.launch {
            try {
                val pkg = sbn.packageName ?: return@launch
                val isLooping = synchronized(activeLoopingPkgs) { activeLoopingPkgs.remove(pkg) }
                if (isLooping) {
                    val turnOffHex = hardwareRepository.getConfig().turnOffHex
                    hardwareRepository.fireStop(turnOffHex, "STOP_LOOP[$pkg]", LedOwner.NOTIFICATION)
                    hardwareRepository.releaseAndRestore(LedOwner.NOTIFICATION)
                }
            } catch (_: Exception) {}
        }
    }

    private fun triggerNotifEffect(pkg: String, hex: String) {
        val wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LEDSync:notif:$pkg")
        wl.acquire(8000L)

        serviceScope.launch {
            try {
                hardwareRepository.fireEffect(hex, "NOTIF_1[$pkg]", LedOwner.NOTIFICATION)
                delay(DOUBLE_FIRE_DELAY_MS)
                hardwareRepository.fireEffect(hex, "NOTIF_2[$pkg]", LedOwner.NOTIFICATION)

                synchronized(lastTriggerPerPkg) {
                    lastTriggerPerPkg[pkg] = System.currentTimeMillis()
                }
                delay(1000L)
                hardwareRepository.releaseAndRestore(LedOwner.NOTIFICATION)
            } finally {
                if (wl.isHeld) wl.release()
            }
        }
    }

    private suspend fun handleBatteryChanged(level: Int, charging: Boolean) {
        if (!hardwareRepository.masterEnabled) return

        val wasCharging = lastChargingState
        if (charging && wasCharging == false) {
            hasFlashedFullThisSession = false
            fireChargeTierFlash(level)
        } else if (!charging && wasCharging == true) {
            chargeTierJob?.cancel()
            hardwareRepository.fireStop(hardwareRepository.getConfig().turnOffHex, "UNPLUG", LedOwner.BATTERY)
            hardwareRepository.releaseAndRestore(LedOwner.BATTERY)
            hasFlashedFullThisSession = false
        }

        if (charging && level >= 100 && !hasFlashedFullThisSession) {
            hasFlashedFullThisSession = true
            fireChargeTierFlash(level)
        }
        lastChargingState = charging

        val lowThreshold = preferencesRepository.battLowThreshold.first()
        val critThreshold = preferencesRepository.battCritThreshold.first()

        if (!charging && level <= critThreshold) {
            val critEffect = preferencesRepository.battCritEffect.first()
            val hex = hardwareRepository.getConfig().ledEffects[critEffect] ?: "00 05 01 03 00 00"
            hardwareRepository.fireEffect(hex, "CRIT_BATT[$level%]", LedOwner.BATTERY)
        } else if (!charging && level <= lowThreshold) {
            val lowEffect = preferencesRepository.battLowEffect.first()
            val hex = hardwareRepository.getConfig().ledEffects[lowEffect] ?: "00 02 00 00 00 00"
            hardwareRepository.fireEffect(hex, "LOW_BATT[$level%]", LedOwner.BATTERY)
        }
    }

    /**
     * Exact 3-tier charge pulse reverse-engineered from stock Transsion Light Services.
     */
    private fun chargeTierHex(level: Int): String = when {
        level < 20 -> "00 02 00 00 00 00"
        level < 60 -> "00 02 01 00 00 00"
        else -> "00 02 02 00 00 00"
    }

    private fun fireChargeTierFlash(level: Int) {
        chargeTierJob?.cancel()
        chargeTierJob = serviceScope.launch {
            hardwareRepository.fireEffect(chargeTierHex(level), "CHARGE_TIER[$level%]", LedOwner.BATTERY)
            delay(3000L)
            hardwareRepository.fireStop(hardwareRepository.getConfig().turnOffHex, "CHARGE_TIER_OFF", LedOwner.BATTERY)
            hardwareRepository.releaseAndRestore(LedOwner.BATTERY)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "LED Sync ROM Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps LED system service active"
            setShowBadge(false)
        }
        notifManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("LED Sync System Service")
            .setContentText("Active · Monitoring battery, calls & notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }
}
