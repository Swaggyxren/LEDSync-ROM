package com.xiannn.ledsync.system.core

/**
 * AW22xxx audio-reactive gain controller.
 *
 * Reverse-engineered from stock Transsion Light Services (TranLightsServiceExtImpl / Utils.java).
 * Stock maps media volume (1 to 15) to a hardware amplification byte: lower volumes
 * require higher amplification (0x32) while loud volumes use lower amplification (0x08).
 */
object AudioGain {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 15
    const val DEFAULT_LEVEL = 8

    // Exact 1 to 15 volume-to-gain inversion map from stock ROM
    val STOCK_VOLUME_GAIN_MAP = mapOf(
        1 to "32",
        2 to "30",
        3 to "29",
        4 to "25",
        5 to "22",
        6 to "1F",
        7 to "1F",
        8 to "17",
        9 to "14",
        10 to "12",
        11 to "10",
        12 to "0D",
        13 to "0B",
        14 to "0A",
        15 to "08"
    )

    fun hexForVolume(volume: Int): String {
        val clamped = volume.coerceIn(1, 15)
        return STOCK_VOLUME_GAIN_MAP[clamped] ?: "14"
    }

    /**
     * Maps percentage reactivity (0-100) from the UI slider to a valid gain hex.
     */
    fun hexForSlider(percent: Int): String {
        // High reactivity = higher amplification (0x32), low reactivity = calm (0x08)
        val invertedIndex = ((100 - percent.coerceIn(0, 100)) * 14 / 100) + 1
        return hexForVolume(invertedIndex)
    }

    /**
     * Hex command sent ~100ms after audio effect start or on volume update: `00 21 GG 00 00 00`
     */
    fun commandForGainHex(gainHex: String): String = "00 21 $gainHex 00 00 00"

    fun commandForVolume(volume: Int): String =
        commandForGainHex(hexForVolume(volume))
}
