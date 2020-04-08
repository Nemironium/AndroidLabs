package io.nemiron.spyapplication.spymodule

import android.content.Context

interface SpyInterface {
    fun getBatteryPct(context: Context): Int

    fun getAvailableMemory(context: Context): Int

    fun getCurrentDate(): String
}