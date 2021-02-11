package com.example.repeatingalarmfoss.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.usecases.BatteryStateHandlingUseCase
import javax.inject.Inject

class LowBatteryTracker : BroadcastReceiver() {
    @Inject
    lateinit var batteryStateHandlingUseCase: BatteryStateHandlingUseCase

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        batteryStateHandlingUseCase.execute()
    }
}