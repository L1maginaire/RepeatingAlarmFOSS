package com.example.repeatingalarmfoss.services

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.repeatingalarmfoss.*
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.base.ID_ALARM_NOTIFIER_SERVICE
import com.example.repeatingalarmfoss.helper.extensions.constructNotification
import com.example.repeatingalarmfoss.helper.extensions.showNotification
import com.example.repeatingalarmfoss.repositories.GetMissedAlarmCounterResult
import com.example.repeatingalarmfoss.repositories.MissedAlarmCounterPreferencesRepository
import com.example.repeatingalarmfoss.screens.alarm.AlarmActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

const val ALARM_ARG_TASK_ID = "com.example.repeatingalarmfoss.services.ALARM_ARG_TASK_ID"

class AlarmNotifierService : ForegroundService() {
    @Inject lateinit var missedAlarmCounterRepo: MissedAlarmCounterPreferencesRepository
    private lateinit var previousTitle: String
    private var previousID: Long = 0L

    override fun getActivity(): Class<out Activity> = AlarmActivity::class.java
    override fun getTitle(intent: Intent?): String = intent?.getStringExtra(ALARM_ARG_TASK_TITLE)!!
    override fun getIcon(): Int = R.drawable.ic_notification_ringing
    override fun getServiceId(): Int = ID_ALARM_NOTIFIER_SERVICE

    override fun onCreate() {
        super.onCreate()
        (application as RepeatingAlarmApp).appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_SHOW_NOTIFICATION -> {
                val taskID = intent.getLongExtra(ALARM_ARG_TASK_ID, 0L)
                require(taskID > 0)
                val title = getTitle(intent)

                if (subscriptions.size() > 0) {
                    subscriptions.clear()
                    showMissedAlarmNotification(previousTitle, previousID)
                    Log.d("a", "zzz $previousID $previousTitle")
                }
                subscriptions += timer.subscribe {
                    showMissedAlarmNotification(title, taskID)
                    stopSelf()
                }

                previousTitle = title
                previousID = taskID
                val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ALARM)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(getIcon())
                    .setContentText(title)
                    .addAction(getCancelAction())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(getFullscreenIntent(intent.extras!!, getActivity()), true)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, getIcon()))
                startForeground(getServiceId(), notificationBuilder.build())
                logger.i { "($title) playing..." }
            }
            ACTION_TERMINATE -> stopSelf()
            ACTION_STOP_FOREGROUND -> stopForeground(true)
            else -> throw IllegalStateException()
        }
        return START_NOT_STICKY
    }

    private fun showMissedAlarmNotification(title: String, taskId: Long) {
        subscriptions += missedAlarmCounterRepo.getAndUpdateMissedAlarmsCounter(previousID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                if (it is GetMissedAlarmCounterResult.Success) {
                    val notificationMessage = when (it.counter) {
                        1 -> String.format(getString(R.string.title_you_have_single_missed_alarm), title)
                        else -> String.format(getString(R.string.title_you_have_missed_alarms), title, it.counter)
                    }
                    logger.i { "missed notification: $notificationMessage" }
                    val notification = constructNotification(CHANNEL_MISSED_ALARM, notificationMessage, R.drawable.ic_launcher_background)
                    showNotification(notification, taskId)
                }
            })
    }
}