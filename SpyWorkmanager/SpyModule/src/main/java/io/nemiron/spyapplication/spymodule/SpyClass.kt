package io.nemiron.spyapplication.spymodule

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.util.*

class SpyClass : SpyInterface {
    override fun getBatteryPct(context: Context): Int {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
            context.registerReceiver(null, it)
        }

        return if (batteryStatus != null) {
            val level: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            (level / scale.toFloat() * 100).toInt()
        } else {
            -1
        }
    }

    override fun getAvailableMemory(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().let { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
            (memoryInfo.availMem / (1024*1024)).toInt()
        }
    }

    override fun getCurrentDate(): String
            = Calendar.getInstance().time.toString()
}