package com.example.repeatingalarmfoss.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.JobIntentService
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.BaseJobIntentService
import com.example.repeatingalarmfoss.base.ID_Job_NextLaunchPreparing
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.receivers.AlarmReceiver
import com.example.repeatingalarmfoss.usecases.NextLaunchPreparationResult
import com.example.repeatingalarmfoss.usecases.NextLaunchPreparationUseCase
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

private const val ARG_TASK = "com.example.repeatingalarmfoss.services.NextLaunchPreparingService(TASK)"
private const val ARG_TASK_BUNDLE = "com.example.repeatingalarmfoss.services.NextLaunchPreparingService(BUNDLE)"

class NextLaunchPreparingService : BaseJobIntentService() {
    @Inject lateinit var nextLaunchPreparationUseCase: NextLaunchPreparationUseCase
    @Inject lateinit var alarmManager: AlarmManager

    override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate()
    }

    override fun onHandleWork(intent: Intent) {
        super.onHandleWork(intent)
        val task = intent.getBundleExtra(ARG_TASK_BUNDLE)!!.getParcelable<Task>(ARG_TASK)!!
        subscriptions += nextLaunchPreparationUseCase.execute(task)
            .subscribe(Consumer {
                if (it is NextLaunchPreparationResult.Success) {
                    alarmManager.set(it.newTask.time.toLong(),
                        PendingIntent.getBroadcast(applicationContext, it.newTask.id.toInt(), AlarmReceiver.createIntent(it.newTask, applicationContext), PendingIntent.FLAG_UPDATE_CURRENT))
                }
            })
    }

    companion object {
        fun enqueueWork(context: Context, serviceClass: Class<out JobIntentService>, task: Task) = enqueueWork(context, serviceClass, ID_Job_NextLaunchPreparing, Intent(context, serviceClass)
                .apply { putExtra(ARG_TASK_BUNDLE, Bundle().apply { putParcelable(ARG_TASK, task) }) })
    }
}