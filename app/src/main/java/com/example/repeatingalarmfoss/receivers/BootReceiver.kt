package com.example.repeatingalarmfoss.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import com.example.repeatingalarmfoss.helper.extensions.scheduleLowBatteryChecker
import com.example.repeatingalarmfoss.services.ReschedulingAlarmsOnBootService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            ReschedulingAlarmsOnBootService.enqueueWork(context.applicationContext, ReschedulingAlarmsOnBootService::class.java)
            context.scheduleLowBatteryChecker()
        }
    }
}