package com.example.repeatingalarmfoss.screens.alarm

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.repeatingalarmfoss.*
import com.example.repeatingalarmfoss.base.ForegroundService.Companion.ACTION_TERMINATE
import com.example.repeatingalarmfoss.base.NotifyingActivity
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.throttleFirst
import com.example.repeatingalarmfoss.services.AlarmNotifierService
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AlarmActivity : NotifyingActivity() {
    @Inject
    lateinit var logger: FlightRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        setContentView(R.layout.activity_alarm)
        setupClicks()
        with(intent.extras!!.getString(ALARM_ARG_TITLE)) {
            tvTaskTitle.text = this
            logger.i { "($this) playing..." }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (event.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_HOME || event.keyCode == KeyEvent.KEYCODE_APP_SWITCH || event.keyCode == KeyEvent.KEYCODE_BACK)) {/*fixme doubtful?*/
            finish()
            startService(Intent(this, AlarmNotifierService::class.java).apply {
                action = ACTION_TERMINATE
            })
            true
        } else super.dispatchKeyEvent(event)

    private fun setupClicks() {
        subscriptions += gotInButton.clicks()
            .throttleFirst()
            .subscribe {
                startService(Intent(this, AlarmNotifierService::class.java).apply {
                    action = ACTION_TERMINATE
                })
                finish()
            }
        subscriptions += Observable.timer(10, TimeUnit.MINUTES, AndroidSchedulers.mainThread())
            .subscribe {
                startService(Intent(this, AlarmNotifierService::class.java).apply {
                    action = ACTION_TERMINATE
                })
                showMissedAlarmNotification()
                finish()
            }
    }

    private fun showMissedAlarmNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(String.format(getString(R.string.title_you_have_missed_alarm), intent.getStringExtra(ALARM_ARG_TITLE)))
            .setPriority(NotificationCompat.PRIORITY_MAX)
        NotificationManagerCompat.from(this).notify(MISSED_ALARM_NOTIFICATION_ID, builder.build())
    }
}