package com.example.repeatingalarmfoss.helper.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build

fun AlarmManager.set(startTime: Long, pendingIntent: PendingIntent) = if (Build.VERSION.SDK_INT < 23) {
    if (Build.VERSION.SDK_INT >= 19) {
        setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
    } else {
        set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
    }
} else {
    setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
}
