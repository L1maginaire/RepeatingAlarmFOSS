package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import com.example.repeatingalarmfoss.CHANNEL_ALARM
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.base.ID_AlarmNotifierService
import com.example.repeatingalarmfoss.ALARM_ARG_TITLE
import com.example.repeatingalarmfoss.screens.alarm.AlarmActivity

class AlarmNotifierService : ForegroundService() {
    override fun getActivity(): Class<out Activity> = AlarmActivity::class.java
    override fun getTitle(intent: Intent?): String = intent?.getStringExtra(ALARM_ARG_TITLE) ?: throw IllegalArgumentException()
    override fun getChannelId(): String = CHANNEL_ALARM
    override fun getIcon(): Int = R.drawable.ic_notification_ringing
    override fun getServiceId(): Int = ID_AlarmNotifierService
}