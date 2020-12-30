package com.example.repeatingalarmfoss.helper.extensions

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_HOUR
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.repeatingalarmfoss.helper.extensions.LongExt.minutesToMilliseconds
import com.example.repeatingalarmfoss.receivers.LowBatteryTracker

const val BATTERY_CHECKER_ID = 999

fun AlarmManager.set(startTime: Long, pendingIntent: PendingIntent) = if (Build.VERSION.SDK_INT < 23) {
    if (Build.VERSION.SDK_INT >= 19) {
        setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
    } else {
        set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
    }
} else {
    setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
}

fun Context.scheduleLowBatteryChecker() = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setRepeating(
    AlarmManager.RTC_WAKEUP,
    System.currentTimeMillis() + minutesToMilliseconds(5),
    INTERVAL_HOUR,
    PendingIntent.getBroadcast(this, BATTERY_CHECKER_ID, Intent(this, LowBatteryTracker::class.java), 0)
)

fun Context.cancelLowBatteryChecker() = (this.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(PendingIntent.getBroadcast(this, BATTERY_CHECKER_ID, Intent(this, LowBatteryTracker::class.java), 0))