package com.xiannn.ledsync.system.core

/**
 * Priority arbitration hierarchy matching stock Transsion Lights Service.
 */
enum class LedOwner(val priority: Int) {
    MUSIC(10),
    BATTERY(20),
    NOTIFICATION(30),
    PHONE_CALL(40),
    MANUAL(50)
}
