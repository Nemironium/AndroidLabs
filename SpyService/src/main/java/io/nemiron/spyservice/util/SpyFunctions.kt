package io.nemiron.spyapplication.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.util.*

class SpyUtils {
    fun getBatteryPct(context: Context): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
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

    fun getAvailableMemory(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().let { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
            (memoryInfo.availMem / (1024*1024)).toInt()
        }
    }

    fun getCurrentDate() = Calendar.getInstance().time.toString()
}
