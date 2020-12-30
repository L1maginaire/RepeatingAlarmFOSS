package com.example.repeatingalarmfoss.base

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.os.IBinder
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.Notifier
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class ForegroundService : Service() {
    companion object {
        const val ACTION_TERMINATE = "ACTION_TERMINATE"
        const val ACTION_STOP_FOREGROUND = "STOP_FOREGROUND"
        const val ACTION_SHOW_NOTIFICATION = "show_notification!"
    }

    @Inject lateinit var notifier: Notifier
    @Inject lateinit var logger: FlightRecorder
    private val disposable = CompositeDisposable()
    private val timer = Observable.timer(10, TimeUnit.MINUTES, AndroidSchedulers.mainThread())
    private lateinit var previousTitle: String

    override fun onBind(intent: Intent?): IBinder? = null
    abstract fun getActivity(): Class<out Activity>
    abstract fun getTitle(intent: Intent?): String
    abstract fun getChannelId(): String
    abstract fun getServiceId(): Int
    @DrawableRes abstract fun getIcon(): Int

    override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate()
        notifier.start()
        Log.d("a", "aaa service created!")
    }

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY.also {
        Log.d("a", "aaa onStart")
        when (intent?.action) {
            ACTION_TERMINATE -> {
                Log.d("a", "aaa terminate")
                stopSelf()
            }
            ACTION_SHOW_NOTIFICATION -> {
                if (disposable.size() > 0) {
                    disposable.clear()
                    showMissedAlarmNotification(previousTitle)
                }
                disposable.add(timer.subscribe {
                    showMissedAlarmNotification(getTitle(intent))
                    stopSelf()
                })

                val notificationBuilder = NotificationCompat.Builder(this, getChannelId())
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(getIcon())
                    .setContentText(getTitle(intent))
                    .addAction(getCancelAction())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(getFullscreenIntent(intent.extras!!, getActivity()), true)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, getIcon()))
                startForeground(getServiceId(), notificationBuilder.build())
                logger.i { "(${getTitle(intent)}) playing..." }
                previousTitle = getTitle(intent)
            }
            ACTION_STOP_FOREGROUND -> stopForeground(true)
            else -> throw IllegalStateException()
        }
    }

    private fun showMissedAlarmNotification(title: String) {
        val builder = NotificationCompat.Builder(this, NotificationsManager.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(String.format(getString(R.string.title_you_have_missed_alarm), title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
        NotificationManagerCompat.from(this).notify(NotificationsManager.MISSED_ALARM_NOTIFICATION_ID, builder.build())
    }

    private fun getCancelAction(): NotificationCompat.Action = NotificationCompat.Action(
        0,
        SpannableString(getString(R.string.dismiss)).apply { setSpan(ForegroundColorSpan(Color.RED), 0, getString(R.string.dismiss).length, 0) },
        PendingIntent.getService(this, 0, Intent(this, this::class.java).apply { this.action = ACTION_TERMINATE }, PendingIntent.FLAG_UPDATE_CURRENT)
    )

    private fun getFullscreenIntent(bundle: Bundle, activity: Class<out Activity>): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, activity)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtras(bundle)
            },
        0
    )

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        notifier.stop()
        Log.d("a", "aaa ${this::javaClass} is destroyed")
    }
}