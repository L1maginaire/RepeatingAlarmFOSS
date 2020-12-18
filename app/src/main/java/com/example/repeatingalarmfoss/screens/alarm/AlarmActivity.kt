package com.example.repeatingalarmfoss.screens.alarm

import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.NotifyingActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.concurrent.TimeUnit

const val ALARM_ARG_TITLE = "arg_title"

class AlarmActivity : NotifyingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        setupClicks()
        tvTaskTitle.text = intent.extras!!.getString(ALARM_ARG_TITLE)
    }

    private fun setupClicks() {
        clicks += cancelButton.clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { finish() }
        clicks += Observable.timer(10, TimeUnit.MINUTES)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showMissedAlarmNotification()
                finish()
            }
    }

    private fun showMissedAlarmNotification() {
        val builder = NotificationCompat.Builder(this, NotificationsManager.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(String.format(getString(R.string.title_you_have_missed_alarm), intent.getStringExtra(ALARM_ARG_TITLE)))
            .setPriority(NotificationCompat.PRIORITY_MAX)
        NotificationManagerCompat.from(this).notify(NotificationsManager.MISSED_ALARM_NOTIFICATION_ID, builder.build())
    }
}