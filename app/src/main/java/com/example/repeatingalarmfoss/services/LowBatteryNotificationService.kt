package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import com.example.repeatingalarmfoss.CHANNEL_BATTERY_LOW_ID
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.base.ID_LowBatteryNotificationService
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity

class LowBatteryNotificationService : ForegroundService()  {
    override fun getIcon(): Int = R.drawable.ic_baseline_battery_alert_24
    override fun getActivity(): Class<out Activity> = LowBatteryNotifierActivity::class.java
    override fun getTitle(intent: Intent?): String = getString(R.string.title_battery_low)
    override fun getChannelId(): String = CHANNEL_BATTERY_LOW_ID
    override fun getServiceId(): Int = ID_LowBatteryNotificationService
}
