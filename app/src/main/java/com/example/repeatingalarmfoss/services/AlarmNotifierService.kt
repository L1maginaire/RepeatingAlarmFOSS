package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.screens.alarm.ALARM_ARG_TITLE
import com.example.repeatingalarmfoss.screens.alarm.AlarmActivity

class AlarmNotifierService : ForegroundService() {
    companion object {
        private const val ID = 101
        const val ARG_TASK_TITLE = "arg_task_title"
    }

    override fun getBundle(intent: Intent?): Bundle = Bundle().apply {
        val taskTitle = intent?.getStringExtra(ARG_TASK_TITLE) ?: throw IllegalArgumentException()
        putString(ALARM_ARG_TITLE, taskTitle)
    }

    override fun getActivity(): Class<out Activity> = AlarmActivity::class.java
    override fun getServiceId(): Int = ID
    override fun getTitle(intent: Intent?): String = intent?.getStringExtra(ARG_TASK_TITLE) ?: throw IllegalArgumentException()
    override fun getChannelId(): String = NotificationsManager.CHANNEL_ALARM
    override fun getIcon(): Int = R.drawable.ic_notification_ringing
}