package com.example.repeatingalarmfoss.screens.low_battery

import android.os.Bundle
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.NotifyingActivity
import com.example.repeatingalarmfoss.helper.rx.DEFAULT_UI_SKIP_DURATION
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_low_battery_notifier.*
import java.util.concurrent.TimeUnit

class LowBatteryNotifierActivity : NotifyingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).apply {
        setContentView(R.layout.activity_low_battery_notifier)
        buttonGotIt.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { finish() }
    }
}