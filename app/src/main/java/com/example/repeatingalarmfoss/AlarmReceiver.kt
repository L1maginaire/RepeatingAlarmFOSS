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
import com.example.repeatingalarmfoss.helper.FixedSizeBitSet
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
            val time = intent.getStringExtra(ALARM_ARG_TIME)
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val chosenWeekDays = FixedSizeBitSet.fromBinaryString(intent.getStringExtra(ALARM_ARG_INTERVAL))
            val hours = time.split(":")[0].toInt()
            val minutes = time.split(":")[1].toInt()
            val timeIsLeft = hours <= Calendar.getInstance().get(Calendar.HOUR_OF_DAY) && minutes <= Calendar.getInstance().get(Calendar.MINUTE)
            val nextLaunchTime = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, if(chosenWeekDays.get(today) && timeIsLeft) today+1 else today)
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

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