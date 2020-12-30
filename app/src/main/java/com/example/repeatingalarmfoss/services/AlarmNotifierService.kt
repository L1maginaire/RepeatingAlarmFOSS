package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.screens.alarm.ALARM_ARG_TITLE
import com.example.repeatingalarmfoss.screens.alarm.AlarmActivity

private const val ID = 1002

class AlarmNotifierService : ForegroundService() {
    override fun getActivity(): Class<out Activity> = AlarmActivity::class.java
    override fun getTitle(intent: Intent?): String = intent?.getStringExtra(ALARM_ARG_TITLE) ?: throw IllegalArgumentException()
    override fun getChannelId(): String = NotificationsManager.CHANNEL_ALARM
    override fun getIcon(): Int = R.drawable.ic_notification_ringing
    override fun getServiceId(): Int = ID
}