package com.example.repeatingalarmfoss.receivers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var taskLocalDataSource: TaskLocalDataSource

    @Inject
    lateinit var logger: FlightRecorder

    @Inject
    lateinit var alarmManager: AlarmManager

    @Suppress("NAME_SHADOWING")
    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        context.scheduleLowBatteryChecker()

        if (intent.action == ACTION_BOOT_COMPLETED) {
            taskLocalDataSource.getAll()
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { logger.e(stackTrace = it.stackTrace) }
                .subscribe({ list -> /*TODO calculate missed and show N notifications*/
                    list.forEachIndexed { index, task ->
                        logger.logScheduledEvent(what = { "Rescheduling ($index) of ${list.size}: " }, `when` = task.time.toLong())
                        val intent = AlarmReceiver.createIntent(task, context)
                        alarmManager.set(task.time.toLong(), PendingIntent.getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    }
                }, { logger.e(stackTrace = it.stackTrace) })
        }
    }
}
