package com.example.repeatingalarmfoss.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.JobIntentService
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.receivers.AlarmReceiver
import com.example.repeatingalarmfoss.usecases.NextLaunchPreparationResult
import com.example.repeatingalarmfoss.usecases.NextLaunchPreparationUseCase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

const val ARG_TASK = "nextLaunchArgTask"
const val ARG_TASK_BUNDLE = "nextLaunchArgTaskBundle"

class NextLaunchPreparingService : JobIntentService() {
    @Inject
    lateinit var nextLaunchPreparationUseCase: NextLaunchPreparationUseCase

    @Inject
    lateinit var alarmManager: AlarmManager

    private val disposable = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { disposable.clear() }

    override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate()
    }

    override fun onHandleWork(intent: Intent) {
        val task = intent.getBundleExtra(ARG_TASK_BUNDLE)!!.getParcelable<Task>(ARG_TASK)!!
        disposable += nextLaunchPreparationUseCase.execute(task)
            .subscribe(Consumer {
                if (it is NextLaunchPreparationResult.Success) {
                    alarmManager.set(
                        it.newTask.time.toLong(),
                        PendingIntent.getBroadcast(applicationContext, it.newTask.id.toInt(), AlarmReceiver.createIntent(it.newTask, applicationContext), PendingIntent.FLAG_UPDATE_CURRENT/*todo check all those flags impact!*/)
                    )
                }
            })
    }

    companion object {
        private var JOB_ID = 1

        fun enqueueWork(context: Context, intent: Intent, task: Task) = enqueueWork(context, NextLaunchPreparingService::class.java, JOB_ID, intent.apply { putExtra(ARG_TASK_BUNDLE, Bundle().apply { putParcelable(ARG_TASK, task) }) })
    }
}