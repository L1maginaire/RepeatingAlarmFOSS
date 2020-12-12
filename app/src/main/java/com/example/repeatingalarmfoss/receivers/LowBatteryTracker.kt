package com.example.repeatingalarmfoss.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.*
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity
import com.example.repeatingalarmfoss.services.LowBatteryNotificationService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val BATTERY_THRESHOLD_PERCENTAGE = 30

class LowBatteryTracker : BroadcastReceiver() {
    @Inject
    @JvmField
    var batteryManager: BatteryManager? = null

    @Inject
    lateinit var logger: FlightRecorder

    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (context.getDefaultSharedPreferences().getBooleanOf(PREF_LOW_BATTERY_DND_AT_NIGHT) && isTimeBetweenTwoTime("00:00", "09:00", SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())))
            return

        (context.applicationContext as RepeatingAlarmApp).apply {
            appComponent.inject(this@LowBatteryTracker)
        }

        (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
            context.registerReceiver(null, intentFilter)?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * 100 / scale.toFloat()
            }
        }?.toInt())?.also {
            logger.i { "${System.currentTimeMillis().toReadableDate() /*todo must be logic of logger*/} Battery level is $it" }
            if (it < BATTERY_THRESHOLD_PERCENTAGE) {
                context.activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
            }
        }
    }
}