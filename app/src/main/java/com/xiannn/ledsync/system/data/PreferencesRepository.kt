package com.xiannn.ledsync.system.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "ledsync_rom_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val KEY_MASTER_ENABLED = booleanPreferencesKey("master_enabled")
        val KEY_AUDIO_LED_ENABLED = booleanPreferencesKey("audio_led_enabled")
        val KEY_AUDIO_LED_DYNAMIC = booleanPreferencesKey("audio_led_dynamic")
        val KEY_AUDIO_LED_REACTIVITY = intPreferencesKey("audio_led_reactivity")
        val KEY_DYNAMIC_VOLUME_TRACKING = booleanPreferencesKey("dynamic_volume_tracking")

        val KEY_FACE_DOWN_MUTE = booleanPreferencesKey("face_down_mute")
        val KEY_RESPECT_DND = booleanPreferencesKey("respect_dnd")
        val KEY_RESPECT_BATTERY_SAVER = booleanPreferencesKey("respect_battery_saver")
        val KEY_CAMERA_SUPPRESSION = booleanPreferencesKey("camera_suppression")

        val KEY_BATT_LOW_THRESHOLD = intPreferencesKey("batt_low_threshold")
        val KEY_BATT_CRIT_THRESHOLD = intPreferencesKey("batt_crit_threshold")
        val KEY_BATT_FULL_THRESHOLD = intPreferencesKey("batt_full_threshold")
        val KEY_BATT_LOW_EFFECT = stringPreferencesKey("batt_low_effect")
        val KEY_BATT_CRIT_EFFECT = stringPreferencesKey("batt_crit_effect")
        val KEY_BATT_FULL_EFFECT = stringPreferencesKey("batt_full_effect")

        val KEY_CALL_EFFECT = stringPreferencesKey("call_effect")
        val KEY_INCLUDE_SYSTEM_APPS = booleanPreferencesKey("include_system_apps")
        val KEY_NOTIF_HEX_MAP = stringPreferencesKey("notif_hex_map")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val masterEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_MASTER_ENABLED] ?: true }
    val audioLedEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUDIO_LED_ENABLED] ?: false }
    val audioLedDynamic: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUDIO_LED_DYNAMIC] ?: true }
    val audioLedReactivity: Flow<Int> = context.dataStore.data.map { it[KEY_AUDIO_LED_REACTIVITY] ?: 60 }
    val dynamicVolumeTracking: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_VOLUME_TRACKING] ?: true }

    val faceDownMuteEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_FACE_DOWN_MUTE] ?: true }
    val respectDnd: Flow<Boolean> = context.dataStore.data.map { it[KEY_RESPECT_DND] ?: true }
    val respectBatterySaver: Flow<Boolean> = context.dataStore.data.map { it[KEY_RESPECT_BATTERY_SAVER] ?: true }
    val cameraSuppressionEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_CAMERA_SUPPRESSION] ?: true }

    val battLowThreshold: Flow<Int> = context.dataStore.data.map { it[KEY_BATT_LOW_THRESHOLD] ?: 20 }
    val battCritThreshold: Flow<Int> = context.dataStore.data.map { it[KEY_BATT_CRIT_THRESHOLD] ?: 10 }
    val battFullThreshold: Flow<Int> = context.dataStore.data.map { it[KEY_BATT_FULL_THRESHOLD] ?: 95 }
    val battLowEffect: Flow<String> = context.dataStore.data.map { it[KEY_BATT_LOW_EFFECT] ?: "Sunrise" }
    val battCritEffect: Flow<String> = context.dataStore.data.map { it[KEY_BATT_CRIT_EFFECT] ?: "Lightning" }
    val battFullEffect: Flow<String> = context.dataStore.data.map { it[KEY_BATT_FULL_EFFECT] ?: "Pure" }

    val callEffect: Flow<String> = context.dataStore.data.map { it[KEY_CALL_EFFECT] ?: "Soft" }
    val includeSystemApps: Flow<Boolean> = context.dataStore.data.map { it[KEY_INCLUDE_SYSTEM_APPS] ?: false }

    val notifHexMap: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_NOTIF_HEX_MAP] ?: ""
        if (raw.isBlank()) emptyMap() else {
            try {
                json.decodeFromString<Map<String, String>>(raw)
            } catch (_: Exception) {
                emptyMap()
            }
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[KEY_THEME_MODE] ?: "SYSTEM" }

    suspend fun setMasterEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_MASTER_ENABLED] = enabled } }
    suspend fun setAudioLedEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_AUDIO_LED_ENABLED] = enabled } }
    suspend fun setAudioLedDynamic(dynamic: Boolean) { context.dataStore.edit { it[KEY_AUDIO_LED_DYNAMIC] = dynamic } }
    suspend fun setAudioLedReactivity(reactivity: Int) { context.dataStore.edit { it[KEY_AUDIO_LED_REACTIVITY] = reactivity } }
    suspend fun setDynamicVolumeTracking(enabled: Boolean) { context.dataStore.edit { it[KEY_DYNAMIC_VOLUME_TRACKING] = enabled } }

    suspend fun setFaceDownMuteEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_FACE_DOWN_MUTE] = enabled } }
    suspend fun setRespectDnd(enabled: Boolean) { context.dataStore.edit { it[KEY_RESPECT_DND] = enabled } }
    suspend fun setRespectBatterySaver(enabled: Boolean) { context.dataStore.edit { it[KEY_RESPECT_BATTERY_SAVER] = enabled } }
    suspend fun setCameraSuppressionEnabled(enabled: Boolean) { context.dataStore.edit { it[KEY_CAMERA_SUPPRESSION] = enabled } }

    suspend fun setBatteryThresholds(low: Int, crit: Int, full: Int) {
        context.dataStore.edit {
            it[KEY_BATT_LOW_THRESHOLD] = low
            it[KEY_BATT_CRIT_THRESHOLD] = crit
            it[KEY_BATT_FULL_THRESHOLD] = full
        }
    }

    suspend fun setBatteryEffects(low: String, crit: String, full: String) {
        context.dataStore.edit {
            it[KEY_BATT_LOW_EFFECT] = low
            it[KEY_BATT_CRIT_EFFECT] = crit
            it[KEY_BATT_FULL_EFFECT] = full
        }
    }

    suspend fun setCallEffect(effect: String) { context.dataStore.edit { it[KEY_CALL_EFFECT] = effect } }
    suspend fun setIncludeSystemApps(include: Boolean) { context.dataStore.edit { it[KEY_INCLUDE_SYSTEM_APPS] = include } }

    suspend fun setAppHex(pkg: String, hex: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_NOTIF_HEX_MAP] ?: ""
            val currentMap = if (raw.isBlank()) mutableMapOf() else {
                try { json.decodeFromString<Map<String, String>>(raw).toMutableMap() } catch (_: Exception) { mutableMapOf() }
            }
            if (hex.isBlank()) currentMap.remove(pkg) else currentMap[pkg] = hex
            prefs[KEY_NOTIF_HEX_MAP] = json.encodeToString(currentMap)
        }
    }

    suspend fun setThemeMode(mode: String) { context.dataStore.edit { it[KEY_THEME_MODE] = mode } }
}
