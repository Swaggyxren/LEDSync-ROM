package com.xiannn.ledsync.system.core

interface DeviceConfig {
    val deviceName: String
    val awPath: String
    val lbCmd: String
    val ledEffects: Map<String, String>
    val loopingPatterns: Set<String>
    val defaultLowEffect: String
    val defaultCriticalEffect: String
    val defaultFullEffect: String
    val turnOffHex: String
}
