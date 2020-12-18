package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity

class LowBatteryNotificationService : ForegroundService()  {
    companion object {
        private const val ID = 102
    }

    override fun getIcon(): Int = R.drawable.ic_baseline_battery_alert_24
    override fun getBundle(intent: Intent?): Bundle? = null
    override fun getActivity(): Class<out Activity> = LowBatteryNotifierActivity::class.java
    override fun getServiceId(): Int = ID
    override fun getTitle(intent: Intent?): String = getString(R.string.title_battery_low)
    override fun getChannelId(): String = NotificationsManager.CHANNEL_BATTERY_LOW_ID
}
