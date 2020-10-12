package com.example.repeatingalarmfoss

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.screens.AlarmActivity
import com.example.repeatingalarmfoss.screens.NotifierService

const val ACTION_RING = "action_ring"
const val ALARM_ARG_INTERVAL = "arg_interval"
const val ALARM_ARG_TITLE = "arg_title"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ALARM_ARG_TITLE)
        if (Build.VERSION.SDK_INT >= 29 && RepeatingAlarmApp.INSTANCE.isAppInForeground.not()) {
            ContextCompat.startForegroundService(context, Intent(context, NotifierService::class.java).apply { putExtra(NotifierService.ARG_TASK_TITLE, title) })
        } else {
            context.startActivity(Intent(context, AlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(NotifierService.ARG_TASK_TITLE, title)
            })
        }

        if (intent.action == ACTION_RING) {
            val interval = intent.getLongExtra(ALARM_ARG_INTERVAL, 0L)
            val newIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_RING
                putExtra(ALARM_ARG_INTERVAL, interval)
                putExtra(ALARM_ARG_TITLE, title)
            }
            (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(SystemClock.elapsedRealtime() + interval, PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }
}