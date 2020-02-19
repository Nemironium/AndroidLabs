package io.nemiron.wifiscanner.utils

fun calculateSignalLevel(level: Int) = when {
    level > -50 -> "Excellent"
    level in -60..-50 -> "Good"
    level in -70..-60 -> "Fair"
    level < -70 -> "Weak"
    else -> "No signal"
}