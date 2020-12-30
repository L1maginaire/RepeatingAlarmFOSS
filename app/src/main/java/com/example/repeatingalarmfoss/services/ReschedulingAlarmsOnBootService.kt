package com.example.repeatingalarmfoss.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.BaseJobIntentService
import com.example.repeatingalarmfoss.base.ID_ReschedulingAlarmsOnBootService
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.receivers.AlarmReceiver
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class ReschedulingAlarmsOnBootService: BaseJobIntentService() {
    @Inject lateinit var taskLocalDataSource: TaskLocalDataSource
    @Inject lateinit var alarmManager: AlarmManager

    override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate()
    }

    override fun onHandleWork(intent: Intent) {
        super.onHandleWork(intent)
        subscriptions += taskLocalDataSource.getAll()
            .doOnError { logger.e(stackTrace = it.stackTrace) }
            .subscribe({ list -> /*TODO calculate missed and show N notifications*/
                list.forEachIndexed { index, task ->
                    logger.logScheduledEvent(what = { "Rescheduling ($index) of ${list.size}: " }, `when` = task.time.toLong())
                    alarmManager.set(task.time.toLong(), PendingIntent.getBroadcast(applicationContext, task.id.toInt(), AlarmReceiver.createIntent(task, applicationContext), 0))
                }
            }, { logger.e(stackTrace = it.stackTrace) })
    }

    companion object {
        fun enqueueWork(context: Context, serviceClass: Class<out JobIntentService>) = enqueueWork(context, serviceClass, ID_ReschedulingAlarmsOnBootService, Intent(context, serviceClass))
    }
}