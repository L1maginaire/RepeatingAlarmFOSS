package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.repeatingalarmfoss.CHANNEL_BATTERY_LOW_ID
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.base.ID_LowBatteryNotificationService
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity

class LowBatteryNotificationService : ForegroundService()  {
    override fun getIcon(): Int = R.drawable.ic_baseline_battery_alert_24
    override fun getActivity(): Class<out Activity> = LowBatteryNotifierActivity::class.java
    override fun getTitle(intent: Intent?): String = getString(R.string.title_battery_low)
    override fun getServiceId(): Int = ID_LowBatteryNotificationService

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_SHOW_NOTIFICATION -> {
                subscriptions.add(timer.subscribe {
                    stopSelf()
                })

                val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_BATTERY_LOW_ID)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(getIcon())
                    .setContentText(getTitle(intent))
                    .addAction(getCancelAction())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(getFullscreenIntent(intent.extras!!, getActivity()), true)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, getIcon()))
                startForeground(getServiceId(), notificationBuilder.build())
            }
            ACTION_TERMINATE -> stopSelf()
            ACTION_STOP_FOREGROUND -> stopForeground(true)
            else -> throw IllegalStateException()
        }
        return START_NOT_STICKY
    }
}
