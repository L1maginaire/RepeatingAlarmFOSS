package com.example.repeatingalarmfoss.screens.alarm

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.repeatingalarmfoss.ALARM_ARG_TASK_TITLE
import com.example.repeatingalarmfoss.CHANNEL_MISSED_ALARM
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.ForegroundService.Companion.ACTION_TERMINATE
import com.example.repeatingalarmfoss.base.NotifyingActivity
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.constructNotification
import com.example.repeatingalarmfoss.helper.extensions.showNotification
import com.example.repeatingalarmfoss.helper.extensions.throttleFirst
import com.example.repeatingalarmfoss.helper.extensions.toast
import com.example.repeatingalarmfoss.repositories.GetMissedAlarmCounterResult
import com.example.repeatingalarmfoss.services.ALARM_ARG_TASK_ID
import com.example.repeatingalarmfoss.services.AlarmNotifierService
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/*TODO rethink how to handle multiple activities overlapping*/
class AlarmActivity : NotifyingActivity() {
    private val viewModel by viewModels<AlarmActivityViewModel> { viewModelFactory }
    @Inject lateinit var logger: FlightRecorder
    private var taskId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        setContentView(R.layout.activity_alarm)
        setupClicks()

        taskId = intent.extras!!.getLong(ALARM_ARG_TASK_ID)
        require(taskId > 0L)

        with(intent.extras!!.getString(ALARM_ARG_TASK_TITLE)) {
            tvTaskTitle.text = this
            logger.i { "($this) playing..." }
        }
        setupViewModelSubscriptions()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(this, { toast(getString(it)) })
        viewModel.getMissedAlarmsCounterEvent.observe(this, { counter ->
            showMissedAlarmNotification(intent.getStringExtra(ALARM_ARG_TASK_TITLE)!!, taskId, counter)
            finish()
            startService(Intent(this, AlarmNotifierService::class.java).apply {
                action = ACTION_TERMINATE
            })
        })
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
            .subscribe { viewModel.getMissedAlarmCounter(taskId) }
    }

    private fun showMissedAlarmNotification(title: String, id: Long, counter: Int) {
        logger.i { "$title missed notification" }
        val notification = constructNotification(CHANNEL_MISSED_ALARM, String.format(getString(R.string.title_you_have_missed_alarms), title, counter), R.drawable.ic_launcher_background)
        showNotification(notification, id)
    }
}