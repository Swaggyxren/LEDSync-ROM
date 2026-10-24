package com.xiannn.ledsync.system.core

class LH8nConfig : DeviceConfig {
    override val deviceName: String = "TECNO POVA 5 PRO 5G (LH8n)"
    override val awPath: String = "/sys/class/leds/aw22xxx_led"
    override val lbCmd: String = "/sys/led/led/tran_led_cmd"

    override val ledEffects: Map<String, String> = mapOf(
        // Calls & Ambient Continuous Looping
        "Soft" to "00 04 00 00 00 00",
        "Racing" to "00 30 01 00 00 00",
        "Dreamy" to "00 03 01 00 00 00",
        "Breath" to "00 20 02 00 00 00",
        "Party" to "00 20 03 00 00 00",

        // One-Shot Notifications
        "Pure" to "00 05 01 00 00 00",
        "Starburst" to "00 05 01 01 00 00",
        "Halo" to "00 05 01 02 00 00",
        "Lightning" to "00 05 01 03 00 00",
        "Sunrise" to "00 05 01 04 00 00",

        // Battery & Charging Tiers
        "Low Battery" to "00 02 00 00 00 00",
        "Charging Tier 1 (0-20%)" to "00 02 00 00 00 00",
        "Charging Tier 2 (20-60%)" to "00 02 01 00 00 00",
        "Charging Tier 3 (60-100%)" to "00 02 02 00 00 00",
        "Full Charge" to "00 02 02 00 00 00",

        // Backwards-compatible aliases
        "Speed" to "00 30 01 00 00 00",
        "Illusion" to "00 03 01 00 00 00",
        "Pureness" to "00 05 01 00 00 00",
        "StarRiver" to "00 05 01 01 00 00",
        "Rise" to "00 05 01 04 00 00",
        "Breathe" to "00 20 02 00 00 00",
        "Charging" to "00 02 01 00 00 00",
        "Charged" to "00 02 02 00 00 00"
    )

    override val loopingPatterns: Set<String> = setOf(
        "Soft",
        "Racing",
        "Dreamy",
        "Breath",
        "Party",
        "Speed",
        "Illusion",
        "Breathe"
    )

    override val defaultLowEffect: String = "Sunrise"
    override val defaultCriticalEffect: String = "Lightning"
    override val defaultFullEffect: String = "Pure"
    override val turnOffHex: String = "00 01 00 00 00 00"
}
