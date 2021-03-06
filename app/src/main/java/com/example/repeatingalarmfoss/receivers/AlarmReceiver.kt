package com.example.repeatingalarmfoss.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.helper.extensions.activityImplicitLaunch
import com.example.repeatingalarmfoss.ALARM_ARG_TASK_TITLE
import com.example.repeatingalarmfoss.screens.alarm.AlarmActivity
import com.example.repeatingalarmfoss.services.ALARM_ARG_TASK_ID
import com.example.repeatingalarmfoss.services.AlarmNotifierService
import com.example.repeatingalarmfoss.services.NextLaunchPreparingService

private const val ACTION_RING = "AlarmReceiver.action_ring"
private const val ALARM_ARG_TASK = "AlarmReceiver.arg_task"
private const val ALARM_BUNDLE = "AlarmReceiver.arg_bundle"

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        fun createIntent(from: Task, context: Context): Intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_RING
            putExtra(ALARM_BUNDLE, Bundle().apply { putParcelable(ALARM_ARG_TASK, from) })
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_RING) {
            val task = intent.getBundleExtra(ALARM_BUNDLE)!!.getParcelable<Task>(ALARM_ARG_TASK)!!
            NextLaunchPreparingService.enqueueWork(context, NextLaunchPreparingService::class.java, task)

            val bundle = Bundle().apply {
                putString(ALARM_ARG_TASK_TITLE, task.description)
                putLong(ALARM_ARG_TASK_ID, task.id)
            }
            context.activityImplicitLaunch(AlarmNotifierService::class.java, AlarmActivity::class.java, bundle)
        }
    }
}

/*todo try daggerGraph*/