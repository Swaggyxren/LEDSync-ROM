package com.xiannn.ledsync.system.core

data class LedEffect(
    val name: String,
    val hexCommand: String,
    val isLooping: Boolean = false,
    val description: String = ""
)
