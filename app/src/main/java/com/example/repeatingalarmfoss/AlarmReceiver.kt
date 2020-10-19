package com.example.repeatingalarmfoss

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.repeatingalarmfoss.screens.AlarmActivity
import com.example.repeatingalarmfoss.screens.NotifierService
import java.util.*
import com.example.repeatingalarmfoss.helper.extensions.set
import java.text.SimpleDateFormat

const val ACTION_RING = "action_ring"
const val ALARM_ARG_INTERVAL = "arg_interval"
const val ALARM_ARG_TIME = "arg_time"
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
            val interval = intent.getStringExtra(ALARM_ARG_INTERVAL)?.split("")?.filter { it.isEmpty().not() }?.map { it.toInt() }
            val nextDay: Int = interval!!.firstOrNull { it >= Calendar.getInstance().get(Calendar.DAY_OF_WEEK) } ?: interval.min()!!
            var nextLaunchTime = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, nextDay)
                set(Calendar.HOUR_OF_DAY, intent.getStringExtra(ALARM_ARG_TIME).split(":")[0].toInt())
                set(Calendar.MINUTE, intent.getStringExtra(ALARM_ARG_TIME).split(":")[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (nextLaunchTime <= System.currentTimeMillis()) {
                nextLaunchTime = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, interval.firstOrNull { it > Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }!!)
                    set(Calendar.HOUR_OF_DAY, intent.getStringExtra(ALARM_ARG_TIME).split(":")[0].toInt())
                    set(Calendar.MINUTE, intent.getStringExtra(ALARM_ARG_TIME).split(":")[1].toInt())
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            val newIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_RING
                putExtra(ALARM_ARG_INTERVAL, intent.getStringExtra(ALARM_ARG_INTERVAL))
                putExtra(ALARM_ARG_TITLE, title)
                putExtra(ALARM_ARG_TIME, intent.getStringExtra(ALARM_ARG_TIME))
            }
            Log.d("ABC", "abc next launch: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK).format(nextLaunchTime)}")
            (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(nextLaunchTime, PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }
}