package com.xiannn.ledsync.system.core

enum class AudioLedMode(val label: String, val hex: String) {
    STATIC("Static White", "00 20 02 00 00 00"),   // Stock Breath mode
    DYNAMIC("Dynamic RGB", "00 20 03 00 00 00")    // Stock Party mode
}
