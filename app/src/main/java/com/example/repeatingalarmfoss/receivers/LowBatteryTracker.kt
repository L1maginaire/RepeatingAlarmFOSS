package com.example.repeatingalarmfoss.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.activityImplicitLaunch
import com.example.repeatingalarmfoss.helper.extensions.toReadableDate
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity
import com.example.repeatingalarmfoss.services.LowBatteryNotificationService
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
        val app = context.applicationContext as RepeatingAlarmApp /*todo as @Inject*/
        app.appComponent.inject(this)

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