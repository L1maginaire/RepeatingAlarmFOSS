package com.example.repeatingalarmfoss.screens.low_battery

import android.os.Bundle
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.NotifyingActivity
import com.example.repeatingalarmfoss.helper.extensions.throttleFirst
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.activity_low_battery_notifier.*

class LowBatteryNotifierActivity : NotifyingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).apply {
        setContentView(R.layout.activity_low_battery_notifier)
        buttonGotIt.clicks()
            .throttleFirst()
            .subscribe { finish() }
    }
}