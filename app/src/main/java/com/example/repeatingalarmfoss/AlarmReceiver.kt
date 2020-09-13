package com.example.repeatingalarmfoss

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.repeatingalarmfoss.helper.extensions.set

const val ACTION_RING = "action_ring"
const val ALARM_ARG_INTERVAL = "arg_interval"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_RING) {
            val interval = intent.getLongExtra(ALARM_ARG_INTERVAL, 0L)
            val newIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_RING
                putExtra(ALARM_ARG_INTERVAL, interval)
            }
            (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(SystemClock.elapsedRealtime() + interval, PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }
}