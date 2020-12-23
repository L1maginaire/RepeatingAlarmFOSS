package com.example.repeatingalarmfoss.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.activityImplicitLaunch
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.screens.alarm.ALARM_ARG_TITLE
import com.example.repeatingalarmfoss.screens.alarm.AlarmActivity
import com.example.repeatingalarmfoss.services.AlarmNotifierService
import com.example.repeatingalarmfoss.usecases.NextLaunchTimeCalculationUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val ACTION_RING = "action_ring"
const val ALARM_ARG_TASK = "arg_task"
const val ALARM_BUNDLE = "arg_bundle"

class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmManager: AlarmManager

    @Inject
    lateinit var logger: FlightRecorder

    @Inject
    lateinit var nextLaunchTimeCalculationUseCase: NextLaunchTimeCalculationUseCase

    @Inject
    lateinit var taskLocalDataSource: TaskLocalDataSource

    private val disposable = CompositeDisposable()

    companion object {
        fun createIntent(from: Task, context: Context): Intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_RING
            putExtra(ALARM_BUNDLE, Bundle().apply { putParcelable(ALARM_ARG_TASK, from) })
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as RepeatingAlarmApp
        (app).appComponent.inject(this)

        if (intent.action == ACTION_RING) {
            val task = intent.getBundleExtra(ALARM_BUNDLE)!!.getParcelable<Task>(ALARM_ARG_TASK)!!
            context.activityImplicitLaunch(AlarmNotifierService::class.java, AlarmActivity::class.java, ALARM_ARG_TITLE, task.description)

            val nextLaunchTime: Long = when (task.repeatingClassifier) {
                RepeatingClassifier.EVERY_X_TIME_UNIT -> nextLaunchTimeCalculationUseCase.getNextLaunchTime(task.time.toLong(), task.repeatingClassifierValue)
                RepeatingClassifier.DAY_OF_WEEK -> nextLaunchTimeCalculationUseCase.getNextLaunchTime(task.time, task.repeatingClassifierValue)
                else -> throw IllegalStateException()
            }

            if (nextLaunchTime <= System.currentTimeMillis()) with("nextLaunchTime is lesser than now") {
                logger.wtf { this }
                throw IllegalStateException(this)
            }

            val newTask = task.copy(time = nextLaunchTime.toString())

            disposable.plusAssign( /*todo to service    */
                taskLocalDataSource.insert(newTask)
                    .timeout(5, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        logger.logScheduledEvent(what = { "Next launch:" }, `when` = nextLaunchTime)
                        alarmManager.set(nextLaunchTime, PendingIntent.getBroadcast(context, task.id.toInt(), createIntent(newTask, context), PendingIntent.FLAG_UPDATE_CURRENT/*todo check all those flags impact!*/))
                    }, {
                        logger.wtf { "${javaClass.simpleName} couldn't save Task into database" }
                    })
            )
        }
    }
}

/*todo try daggerGraph*/